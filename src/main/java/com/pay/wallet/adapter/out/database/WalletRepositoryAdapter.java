package com.pay.wallet.adapter.out.database;

import com.pay.wallet.application.port.out.WalletRepository;
import com.pay.wallet.domain.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Override
    public boolean existsByUserId(Long userId) {
        return walletJpaRepository.existsByUserId(userId);
    }

    @Override
    public Optional<Wallet> findByUserId(Long userId) {
        return walletJpaRepository.findByUserId(userId)
                .map(WalletEntity::toDomain);
    }
}
