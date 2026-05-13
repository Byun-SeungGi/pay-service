package com.pay.wallet.application.port.out;

public record TossCancelResult(
        String paymentKey,
        String rawResponse
) {
}
