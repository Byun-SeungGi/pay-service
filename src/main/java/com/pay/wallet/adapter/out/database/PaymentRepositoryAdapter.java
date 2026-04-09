package com.pay.wallet.adapter.out.database;

import com.pay.wallet.application.port.out.PaymentRepository;
import com.pay.wallet.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}
