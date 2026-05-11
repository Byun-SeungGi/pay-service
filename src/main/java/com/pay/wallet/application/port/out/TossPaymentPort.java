package com.pay.wallet.application.port.out;

import java.math.BigDecimal;

public interface TossPaymentPort {

    TossConfirmResult confirm(String paymentKey, String orderId, BigDecimal amount);

    TossCancelResult cancel(String paymentKey, String cancelReason, BigDecimal cancelAmount, String idempotencyKey);

    TossPaymentStatus getPayment(String paymentKey);
}
