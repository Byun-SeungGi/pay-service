package com.pay.wallet.adapter.out.database;

import com.pay.wallet.application.port.out.PaymentRepository;
import com.pay.wallet.domain.Payment;
import com.pay.wallet.domain.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = PaymentEntity.fromDomain(payment);
        PaymentEntity saved = paymentJpaRepository.save(entity);

        return saved.toDomain();
    }

    @Override
    public List<Payment> findByUserId(Long userId) {
        return paymentJpaRepository.findByUserId(userId)
                .stream()
                .map(PaymentEntity::toDomain)
                .toList();
    }

    @Override
    public Payment findById(Long paymentId) {
        return paymentJpaRepository.findById(paymentId)
                .map(PaymentEntity::toDomain)
                .orElseThrow(() -> new IllegalStateException("결제를 찾을 수 없습니다."));
    }

    @Override
    public List<Payment> findReadyOlderThan(Duration duration) {
        LocalDateTime cutoff = LocalDateTime.now().minus(duration);
        return paymentJpaRepository.findByStatusAndCreatedAtBefore(PaymentStatus.READY, cutoff)
                .stream()
                .map(PaymentEntity::toDomain)
                .toList();
    }
}
