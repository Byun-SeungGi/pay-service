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
import java.time.Instant;
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
        String key = createUuidV7(Instant.now());
        Wallet wallet = Wallet.create(1L);
        given(walletRepository.findByUserIdForUpdate(1L)).willReturn(Optional.of(wallet));
        given(walletRepository.save(any(Wallet.class))).willAnswer(inv -> inv.getArgument(0));
        given(ledgerRepository.existsByTransactionId(anyString())).willReturn(false);
        given(ledgerRepository.save(any(Ledger.class))).willAnswer(inv -> inv.getArgument(0));

        Wallet result = walletChargeService.charge(1L, BigDecimal.valueOf(5000), key);

        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        verify(walletRepository, times(1)).save(any(Wallet.class));
        verify(ledgerRepository, times(1)).save(any(Ledger.class));
    }

    @Test
    @DisplayName("충전 실패 - 지갑 없음")
    void charge_fail_wallet_not_found() {
        String key = createUuidV7(Instant.now());
        given(walletRepository.findByUserIdForUpdate(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> walletChargeService.charge(1L, BigDecimal.valueOf(5000), key))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지갑이 존재하지 않습니다.");

        verify(walletRepository, times(0)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("중복 충전 요청 - 멱등성 체크")
    void charge_duplicate_request() {
        String key = createUuidV7(Instant.now());
        Wallet wallet = Wallet.create(1L);
        given(walletRepository.findByUserIdForUpdate(1L)).willReturn(Optional.of(wallet));
        given(ledgerRepository.existsByTransactionId("1::" + key)).willReturn(true);

        Wallet result = walletChargeService.charge(1L, BigDecimal.valueOf(5000), key);

        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(ledgerRepository, never()).save(any(Ledger.class));
    }

    @Test
    @DisplayName("같은 멱등성 키 다른 유저 - 각각 충전 성공")
    void charge_same_key_different_user() {
        String key = createUuidV7(Instant.now());
        Wallet wallet1 = Wallet.create(1L);
        Wallet wallet2 = Wallet.create(2L);

        given(walletRepository.findByUserIdForUpdate(1L)).willReturn(Optional.of(wallet1));
        given(walletRepository.findByUserIdForUpdate(2L)).willReturn(Optional.of(wallet2));
        given(ledgerRepository.existsByTransactionId("1::" + key)).willReturn(false);
        given(ledgerRepository.existsByTransactionId("2::" + key)).willReturn(false);
        given(walletRepository.save(any(Wallet.class))).willAnswer(inv -> inv.getArgument(0));
        given(ledgerRepository.save(any(Ledger.class))).willAnswer(inv -> inv.getArgument(0));

        Wallet result1 = walletChargeService.charge(1L, BigDecimal.valueOf(5000), key);
        Wallet result2 = walletChargeService.charge(2L, BigDecimal.valueOf(3000), key);

        assertThat(result1.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(result2.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(3000));
    }

    private String createUuidV7(Instant instant) {
        String hex = String.format("%012x", instant.toEpochMilli());
        return hex.substring(0, 8) + "-" + hex.substring(8, 12) + "-7000-8000-000000000000";
    }
}