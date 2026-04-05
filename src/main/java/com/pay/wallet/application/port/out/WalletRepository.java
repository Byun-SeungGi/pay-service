package com.pay.wallet.application.port.out;

import com.pay.wallet.domain.Wallet;

import java.util.Optional;

public interface WalletRepository {

    Wallet save(Wallet wallet);

    boolean existsByUserId(Long userId);

    Optional<Wallet> findByUserId(Long userId);

    Optional<Wallet> findByUserIdForUpdate(Long userId);
}
