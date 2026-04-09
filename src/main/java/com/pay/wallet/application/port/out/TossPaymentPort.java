package com.pay.wallet.application.port.out;

import java.math.BigDecimal;

public interface TossPaymentPort {

    String confirm(String paymentKey, String orderId, BigDecimal amount);
}
