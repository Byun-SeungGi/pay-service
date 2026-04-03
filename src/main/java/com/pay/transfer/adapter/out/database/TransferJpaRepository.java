package com.pay.transfer.adapter.out.database;

import com.pay.transfer.domain.Transfer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransferJpaRepository extends JpaRepository<TransferEntity, Long> {

    Optional<TransferEntity> findByTransactionId(String transactionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TransferEntity t WHERE t.transferId = :id")
    Optional<TransferEntity> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT t FROM TransferEntity t WHERE t.status = 'WITHDRAWN' AND t.updatedAt < :before")
    List<TransferEntity> findStuckWithdrawnTransfer(@Param("before") LocalDateTime before);
}
