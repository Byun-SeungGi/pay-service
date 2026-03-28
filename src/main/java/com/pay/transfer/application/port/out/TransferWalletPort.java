package com.pay.transfer.application.port.out;

import java.math.BigDecimal;

public interface TransferWalletPort {

    void withdraw(Long userId, BigDecimal amount);

    void deposit(Long userId, BigDecimal amount);
}
