package com.pay.wallet.adapter.out.database;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletJpaRepository extends JpaRepository<WalletEntity, Long> {
}
