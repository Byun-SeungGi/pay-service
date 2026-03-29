package com.pay.transfer.application.port.out;

import com.pay.wallet.domain.Wallet;

import java.math.BigDecimal;

public interface TransferWalletPort {

    Wallet withdraw(Long userId, BigDecimal amount);

    Wallet deposit(Long userId, BigDecimal amount);
}
