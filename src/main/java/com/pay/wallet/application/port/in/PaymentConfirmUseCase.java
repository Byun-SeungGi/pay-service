package com.pay.wallet.application.port.in;

import com.pay.wallet.domain.Payment;

import java.math.BigDecimal;

public interface PaymentConfirmUseCase {
    Payment confirm(String paymentKey, String orderId, Long userId, BigDecimal amount);
}
