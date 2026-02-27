package com.pay.wallet.application.service;

import com.pay.wallet.application.port.out.WalletRepository;
import com.pay.wallet.domain.Wallet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @InjectMocks
    private WalletService walletService;

    @Mock
    private WalletRepository walletRepository;

    @Test
    @DisplayName("지갑생성 성공")
    void createWallet_success() {

        given(walletRepository.existsByUserId(1L)).willReturn(false);
        walletService.createWallet(1L);
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("지갑생성 실패 - 이미 존재")
    void createWallet_fail() {
        given(walletRepository.existsByUserId(1L)).willReturn(true);

        assertThatThrownBy(() -> walletService.createWallet(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 지갑이 생성되었습니다.");

        verify(walletRepository, times(0)).save(any(Wallet.class));
    }
}
