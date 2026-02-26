package com.pay.wallet.adapter.out.database;

import com.pay.wallet.application.port.out.WalletRepository;
import com.pay.wallet.domain.Wallet;
import org.springframework.stereotype.Service;

@Service
public class WalletRepositoryAdapter implements WalletRepository {

    @Override
    public Wallet save(Wallet wallet) {

    }
}
