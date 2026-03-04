package com.pay.wallet.application.domain;

import com.pay.wallet.domain.Wallet;
import com.pay.wallet.domain.WalletStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletTest {

    @Test
    @DisplayName("충전 성공")
    void charge_success() {
        Wallet wallet = createActiveWallet(BigDecimal.valueOf(10000));

        Wallet charge = wallet.charge(BigDecimal.valueOf(5000));

        assertThat(charge.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(15000));
    }

    @Test
    @DisplayName("충전 실패 - 충전금액 0원")
    void charge_fail_zero_amount() {
        Wallet wallet = createActiveWallet(BigDecimal.valueOf(10000));

        assertThatThrownBy(() -> wallet.charge(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전 금액은 0원보다 커야 합니다.");
    }

    @Test
    @DisplayName("충전 실패 - 금액이 null")
    void charge_fail_null_amount() {
        Wallet wallet = createActiveWallet(BigDecimal.valueOf(10000));

        assertThatThrownBy(() -> wallet.charge(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전 금액은 0원보다 커야 합니다.");
    }

    @Test
    @DisplayName("충전 실패 - 음수 금액")
    void charge_fail_negative_amount() {
        Wallet wallet = createActiveWallet(BigDecimal.valueOf(10000));

        assertThatThrownBy(() -> wallet.charge(BigDecimal.valueOf(-1000)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전 금액은 0원보다 커야 합니다.");
    }

    @Test
    @DisplayName("충전 실패 - 정지된 지갑")
    void charge_fail_suspended_wallet() {
        Wallet wallet = createWalletWithStatus(WalletStatus.SUSPENDED);

        assertThatThrownBy(() -> wallet.charge(BigDecimal.valueOf(5000)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("사용할 수 있는 지갑이 아닙니다.");
    }

    @Test
    @DisplayName("충전 실패 - 탈퇴된 지갑")
    void charge_fail_withdrawn_wallet() {
        Wallet wallet = createWalletWithStatus(WalletStatus.WITHDRAWN);

        assertThatThrownBy(() -> wallet.charge(BigDecimal.valueOf(5000)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("사용할 수 있는 지갑이 아닙니다.");
    }

    private Wallet createActiveWallet(BigDecimal balance) {
        return Wallet.restore(1L, UUID.randomUUID(), 1L, balance, 0L,
                WalletStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
    }

    private Wallet createWalletWithStatus(WalletStatus status) {
        return Wallet.restore(1L, UUID.randomUUID(), 1L, BigDecimal.valueOf(10000), 0L,
                status, LocalDateTime.now(), LocalDateTime.now());
    }
}
