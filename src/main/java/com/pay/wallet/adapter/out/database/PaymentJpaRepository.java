package com.pay.wallet.adapter.out.database;

import com.pay.wallet.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findByUserId(Long userId);
}