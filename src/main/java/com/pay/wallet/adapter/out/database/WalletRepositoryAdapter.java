package com.pay.wallet.adapter.out.database;

import com.pay.wallet.application.port.out.WalletRepository;
import com.pay.wallet.domain.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository
@RequiredArgsConstructor
public class WalletRepositoryAdapter implements WalletRepository {

    private final WalletJpaRepository walletJpaRepository;

    @Override
    public Wallet save(Wallet wallet) {
        WalletEntity entity = WalletEntity.fromDomain(wallet);
        WalletEntity saved = walletJpaRepository.save(entity);

        return saved.toDomain();
    }
}
