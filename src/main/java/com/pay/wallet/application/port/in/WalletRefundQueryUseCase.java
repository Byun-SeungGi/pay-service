package com.pay.wallet.application.port.in;

public interface WalletRefundQueryUseCase {

    boolean isAlreadyRefunded(Long userId, String idempotencyKey);
}
