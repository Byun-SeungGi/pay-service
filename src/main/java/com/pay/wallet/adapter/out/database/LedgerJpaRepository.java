package com.pay.wallet.adapter.out.database;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerJpaRepository extends JpaRepository<LedgerEntity, Long> {

    List<LedgerEntity> findByWalletId(Long walletId);
}
