package com.pay.wallet.adapter.out.database;

import com.pay.wallet.domain.Payment;
import com.pay.wallet.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findByUserId(Long userId);

    List<PaymentEntity> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime createdAt);
}