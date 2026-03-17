package com.pay.wallet.application.service;

import com.pay.wallet.application.port.out.LedgerRepository;
import com.pay.wallet.application.port.out.WalletRepository;
import com.pay.wallet.domain.Ledger;
import com.pay.wallet.domain.Wallet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class WalletChargeServiceTest {

    @InjectMocks
    private WalletChargeService walletChargeService;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private LedgerRepository ledgerRepository;

    @Test
    @DisplayName("충전 성공")
    void charge_success() {
        Wallet wallet = Wallet.create(1L);
        given(walletRepository.findByUserId(1L)).willReturn(Optional.of(wallet));
        given(walletRepository.save(any(Wallet.class))).willAnswer(inv -> inv.getArgument(0));
        given(ledgerRepository.existsByTransactionId(anyString())).willReturn(false);
        given(ledgerRepository.save(any(Ledger.class))).willAnswer(inv -> inv.getArgument(0));

        Wallet result = walletChargeService.charge(1L, BigDecimal.valueOf(5000), "transactionId");

        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        verify(walletRepository, times(1)).save(any(Wallet.class));
        verify(ledgerRepository, times(1)).save(any(Ledger.class));
    }

    @Test
    @DisplayName("충전 실패 - 지갑 없음")
    void charge_fail_wallet_not_found() {
        given(walletRepository.findByUserId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> walletChargeService.charge(1L, BigDecimal.valueOf(5000), "transactionId"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지갑이 존재하지 않습니다.");

        verify(walletRepository, times(0)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("중복 충전 요청 - 멱등성 체크")
    void charge_duplicate_request() {
        Wallet wallet = Wallet.create(1L);
        given(walletRepository.findByUserId(1L)).willReturn(Optional.of(wallet));
        given(ledgerRepository.existsByTransactionId("duplicateId")).willReturn(true);  // 중복임

        Wallet result = walletChargeService.charge(1L, BigDecimal.valueOf(5000), "duplicateId");

        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);  // 충전 안 됨
        verify(walletRepository, never()).save(any(Wallet.class));  // 지갑 저장 안 함
        verify(ledgerRepository, never()).save(any(Ledger.class));  // 원장 저장 안 함
    }
}