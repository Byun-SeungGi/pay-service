package com.pay.wallet.adapter.out.database;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransferJpaRepository extends JpaRepository<TransferEntity, Long> {

    Optional<TransferEntity> findByTransactionId(String transactionId);
}
