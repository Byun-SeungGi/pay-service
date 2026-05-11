package com.pay.wallet.application.port.out;

public record TossConfirmResult(
        String paymentKey,
        String rawResponse
) {
}
