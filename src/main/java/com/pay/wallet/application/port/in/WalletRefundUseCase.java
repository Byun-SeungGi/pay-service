package com.pay.wallet.application.port.in;

import com.pay.wallet.domain.Wallet;

import java.math.BigDecimal;

public interface WalletRefundUseCase {

    Wallet refund(Long userId, BigDecimal amount, String transactionId);
}
