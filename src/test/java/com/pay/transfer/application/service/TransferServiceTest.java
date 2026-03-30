package com.pay.transfer.application.service;

import com.pay.transfer.application.port.out.TransferLedgerPort;
import com.pay.transfer.application.port.out.TransferRepository;
import com.pay.transfer.application.port.out.TransferWalletPort;
import com.pay.transfer.domain.Transfer;
import com.pay.transfer.domain.TransferStatus;
import com.pay.wallet.domain.Ledger;
import com.pay.wallet.domain.Wallet;
import com.pay.wallet.domain.WalletStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @InjectMocks
    private TransferService transferService;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private TransferWalletPort transferWalletPort;

    @Mock
    private TransferLedgerPort transferLedgerPort;

    @Test
    @DisplayName("송금 성공")
    void transfer_success() {
        // given
        given(transferRepository.findByTransactionId("tx-001")).willReturn(Optional.empty());
        given(transferRepository.save(any(Transfer.class))).willAnswer(inv -> {
            Transfer t = inv.getArgument(0);
            return Transfer.restore(1L, t.getSenderUserId(), t.getReceiverUserId(),
                    t.getAmount(), t.getTransactionId(), t.getStatus(), t.getMemo(),
                    t.getCreatedAt(), t.getUpdatedAt());
        });

        Wallet senderWallet = createWallet(1L, 1L, BigDecimal.valueOf(5000));
        Wallet receiverWallet = createWallet(2L, 2L, BigDecimal.valueOf(20000));
        given(transferWalletPort.withdraw(1L, BigDecimal.valueOf(10000))).willReturn(senderWallet);
        given(transferWalletPort.deposit(2L, BigDecimal.valueOf(10000))).willReturn(receiverWallet);
        given(transferLedgerPort.save(any(Ledger.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        Transfer result = transferService.transfer(1L, 2L, BigDecimal.valueOf(10000), "tx-001", "테스트 송금");

        // then
        assertThat(result.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        verify(transferRepository, times(3)).save(any(Transfer.class));
        verify(transferWalletPort).withdraw(1L, BigDecimal.valueOf(10000));
        verify(transferWalletPort).deposit(2L, BigDecimal.valueOf(10000));
        verify(transferLedgerPort, times(2)).save(any(Ledger.class));
    }

    @Test
    @DisplayName("송금 실패 - 중복 요청")
    void transfer_fail_duplicate_request() {
        // given
        Transfer existingTransfer = Transfer.restore(1L, 1L, 2L, BigDecimal.valueOf(10000),
                "tx-001", TransferStatus.COMPLETED, "메모", LocalDateTime.now(), LocalDateTime.now());
        given(transferRepository.findByTransactionId("tx-001")).willReturn(Optional.of(existingTransfer));

        // when & then
        assertThatThrownBy(() -> transferService.transfer(1L, 2L, BigDecimal.valueOf(10000), "tx-001", "메모"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 처리된 송금 요청입니다.");

        verify(transferRepository, never()).save(any(Transfer.class));
        verify(transferWalletPort, never()).withdraw(anyLong(), any());
        verify(transferWalletPort, never()).deposit(anyLong(), any());
    }

    @Test
    @DisplayName("송금 실패 - 출금 실패")
    void transfer_fail_withdraw_failure() {
        // given
        given(transferRepository.findByTransactionId("tx-001")).willReturn(Optional.empty());
        given(transferRepository.save(any(Transfer.class))).willAnswer(inv -> {
            Transfer t = inv.getArgument(0);
            return Transfer.restore(1L, t.getSenderUserId(), t.getReceiverUserId(),
                    t.getAmount(), t.getTransactionId(), t.getStatus(), t.getMemo(),
                    t.getCreatedAt(), t.getUpdatedAt());
        });
        given(transferWalletPort.withdraw(1L, BigDecimal.valueOf(10000)))
                .willThrow(new IllegalArgumentException("잔액이 부족합니다."));

        // when
        Transfer result = transferService.transfer(1L, 2L, BigDecimal.valueOf(10000), "tx-001", "메모");

        // then
        assertThat(result.getStatus()).isEqualTo(TransferStatus.FAILED);
        verify(transferRepository, times(2)).save(any(Transfer.class));
        verify(transferWalletPort, never()).deposit(anyLong(), any());
        verify(transferLedgerPort, never()).save(any(Ledger.class));
    }

    @Test
    @DisplayName("송금 실패 - 입금 실패 후 환불")
    void transfer_fail_deposit_failure_refund() {
        // given
        given(transferRepository.findByTransactionId("tx-001")).willReturn(Optional.empty());
        given(transferRepository.save(any(Transfer.class))).willAnswer(inv -> {
            Transfer t = inv.getArgument(0);
            return Transfer.restore(1L, t.getSenderUserId(), t.getReceiverUserId(),
                    t.getAmount(), t.getTransactionId(), t.getStatus(), t.getMemo(),
                    t.getCreatedAt(), t.getUpdatedAt());
        });

        Wallet senderWallet = createWallet(1L, 1L, BigDecimal.valueOf(5000));
        given(transferWalletPort.withdraw(1L, BigDecimal.valueOf(10000))).willReturn(senderWallet);
        given(transferWalletPort.deposit(2L, BigDecimal.valueOf(10000)))
                .willThrow(new RuntimeException("입금 처리 실패"));
        given(transferWalletPort.deposit(1L, BigDecimal.valueOf(10000))).willReturn(senderWallet);
        given(transferLedgerPort.save(any(Ledger.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        Transfer result = transferService.transfer(1L, 2L, BigDecimal.valueOf(10000), "tx-001", "메모");

        // then
        assertThat(result.getStatus()).isEqualTo(TransferStatus.REFUNDED);
        verify(transferRepository, times(3)).save(any(Transfer.class));
        verify(transferWalletPort).deposit(1L, BigDecimal.valueOf(10000));
        verify(transferLedgerPort, times(1)).save(any(Ledger.class));
    }

    private Wallet createWallet(Long walletId, Long userId, BigDecimal balance) {
        return Wallet.restore(walletId, UUID.randomUUID(), userId, balance, 0L,
                WalletStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
    }
}
