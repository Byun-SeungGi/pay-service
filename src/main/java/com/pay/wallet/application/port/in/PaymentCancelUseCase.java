package com.pay.wallet.application.port.in;

import com.pay.wallet.domain.Payment;

import java.math.BigDecimal;

public interface PaymentCancelUseCase {
    Payment cancel(Long paymentId, BigDecimal cancelAmount, Long userId, String idempotencyKey, String cancelReason);
}
