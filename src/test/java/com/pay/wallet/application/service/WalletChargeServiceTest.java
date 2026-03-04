package com.pay.wallet.application.service;

import com.pay.wallet.application.port.out.WalletRepository;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WalletChargeServiceTest {

    @InjectMocks
    private WalletChargeService walletChargeService;

    @Mock
    private WalletRepository walletRepository;

    @Test
    @DisplayName("충전 성공")
    void charge_success() {
        Wallet wallet = Wallet.create(1L);
        given(walletRepository.findByUserId(1L)).willReturn(Optional.of(wallet));
        given(walletRepository.save(any(Wallet.class))).willAnswer(inv -> inv.getArgument(0));

        Wallet result = walletChargeService.charge(1L, BigDecimal.valueOf(5000));

        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("충전 실패 - 지갑 없음")
    void charge_fail_wallet_not_found() {
        given(walletRepository.findByUserId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> walletChargeService.charge(1L, BigDecimal.valueOf(5000)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지갑이 존재하지 않습니다.");

        verify(walletRepository, times(0)).save(any(Wallet.class));
    }
}