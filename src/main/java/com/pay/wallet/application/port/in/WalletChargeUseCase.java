package com.pay.wallet.application.port.in;

import com.pay.wallet.domain.Wallet;

import java.math.BigDecimal;

public interface WalletChargeUseCase {

    Wallet charge(Long userId, BigDecimal amount, String transactionId);
}
