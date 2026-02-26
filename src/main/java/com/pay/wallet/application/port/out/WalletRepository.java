package com.pay.wallet.application.port.out;

import com.pay.wallet.domain.Wallet;

public interface WalletRepository {

    Wallet save(Wallet wallet);
}
