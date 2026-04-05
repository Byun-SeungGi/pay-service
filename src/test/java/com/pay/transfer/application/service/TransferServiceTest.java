package com.pay.transfer.application.service;

import com.pay.wallet.application.port.out.LedgerRepository;
import com.pay.wallet.application.port.out.TransferRepository;
import com.pay.wallet.application.port.out.WalletRepository;
import com.pay.wallet.application.service.TransferService;
import com.pay.wallet.domain.Transfer;
import com.pay.wallet.domain.TransferStatus;
import com.pay.wallet.domain.Ledger;
import com.pay.wallet.domain.Wallet;
import com.pay.wallet.domain.WalletStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
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
    private WalletRepository walletRepository;

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        willAnswer(inv -> {
            TransactionCallback<?> callback = inv.getArgument(0);
            return callback.doInTransaction(null);
        }).given(transactionTemplate).execute(any());
    }

    @Test
    @DisplayName("송금 성공")
    void transfer_success() {
        // given
        given(transferRepository.findByTransactionId("1::tx-001")).willReturn(Optional.empty());
        given(transferRepository.save(any(Transfer.class))).willAnswer(inv -> {
            Transfer t = inv.getArgument(0);
            return Transfer.restore(1L, t.getSenderUserId(), t.getReceiverUserId(),
                    t.getAmount(), t.getTransactionId(), t.getStatus(), t.getMemo(),
                    t.getCreatedAt(), t.getUpdatedAt());
        });

        Wallet senderWallet = createWallet(1L, 1L, BigDecimal.valueOf(50000));
        Wallet receiverWallet = createWallet(2L, 2L, BigDecimal.valueOf(20000));
        given(walletRepository.findByUserIdForUpdate(1L)).willReturn(Optional.of(senderWallet));
        given(walletRepository.findByUserIdForUpdate(2L)).willReturn(Optional.of(receiverWallet));
        given(walletRepository.save(any(Wallet.class))).willAnswer(inv -> inv.getArgument(0));
        given(ledgerRepository.save(any(Ledger.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        Transfer result = transferService.transfer(1L, 2L, BigDecimal.valueOf(10000), "tx-001", "테스트 송금");

        // then
        assertThat(result.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        verify(transferRepository, times(2)).save(any(Transfer.class));
        verify(walletRepository).findByUserIdForUpdate(1L);
        verify(walletRepository).findByUserIdForUpdate(2L);
        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(ledgerRepository, times(2)).save(any(Ledger.class));
    }

    @Test
    @DisplayName("송금 실패 - 중복 요청")
    void transfer_fail_duplicate_request() {
        // given
        Transfer existingTransfer = Transfer.restore(1L, 1L, 2L, BigDecimal.valueOf(10000),
                "1::tx-001", TransferStatus.COMPLETED, "메모", LocalDateTime.now(), LocalDateTime.now());
        given(transferRepository.findByTransactionId("1::tx-001")).willReturn(Optional.of(existingTransfer));

        // when & then
        assertThatThrownBy(() -> transferService.transfer(1L, 2L, BigDecimal.valueOf(10000), "tx-001", "메모"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 처리된 송금 요청입니다.");

        verify(transferRepository, never()).save(any(Transfer.class));
        verify(walletRepository, never()).findByUserIdForUpdate(anyLong());
    }

    @Test
    @DisplayName("송금 실패 - 출금 실패")
    void transfer_fail_withdraw_failure() {
        // given
        given(transferRepository.findByTransactionId("1::tx-001")).willReturn(Optional.empty());
        given(transferRepository.save(any(Transfer.class))).willAnswer(inv -> {
            Transfer t = inv.getArgument(0);
            return Transfer.restore(1L, t.getSenderUserId(), t.getReceiverUserId(),
                    t.getAmount(), t.getTransactionId(), t.getStatus(), t.getMemo(),
                    t.getCreatedAt(), t.getUpdatedAt());
        });
        given(walletRepository.findByUserIdForUpdate(1L)).willReturn(Optional.empty());

        // when
        Transfer result = transferService.transfer(1L, 2L, BigDecimal.valueOf(10000), "tx-001", "메모");

        // then
        assertThat(result.getStatus()).isEqualTo(TransferStatus.FAILED);
        verify(transferRepository, times(2)).save(any(Transfer.class));
        verify(walletRepository, never()).findByUserIdForUpdate(2L);
        verify(ledgerRepository, never()).save(any(Ledger.class));
    }

    @Test
    @DisplayName("송금 실패 - 입금 실패")
    void transfer_fail_deposit_failure() {
        // given
        given(transferRepository.findByTransactionId("1::tx-001")).willReturn(Optional.empty());
        given(transferRepository.save(any(Transfer.class))).willAnswer(inv -> {
            Transfer t = inv.getArgument(0);
            return Transfer.restore(1L, t.getSenderUserId(), t.getReceiverUserId(),
                    t.getAmount(), t.getTransactionId(), t.getStatus(), t.getMemo(),
                    t.getCreatedAt(), t.getUpdatedAt());
        });

        Wallet senderWallet = createWallet(1L, 1L, BigDecimal.valueOf(50000));
        given(walletRepository.findByUserIdForUpdate(1L)).willReturn(Optional.of(senderWallet));
        given(walletRepository.findByUserIdForUpdate(2L)).willReturn(Optional.empty());
        given(walletRepository.save(any(Wallet.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        Transfer result = transferService.transfer(1L, 2L, BigDecimal.valueOf(10000), "tx-001", "메모");

        // then
        assertThat(result.getStatus()).isEqualTo(TransferStatus.FAILED);
        verify(transferRepository, times(2)).save(any(Transfer.class));
        verify(ledgerRepository, never()).save(any(Ledger.class));
    }

    private Wallet createWallet(Long walletId, Long userId, BigDecimal balance) {
        return Wallet.restore(walletId, UUID.randomUUID(), userId, balance, 0L,
                WalletStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
    }
}
