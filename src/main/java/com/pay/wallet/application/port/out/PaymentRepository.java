package com.pay.wallet.application.port.out;

import com.pay.wallet.domain.Payment;

import java.time.Duration;
import java.util.List;

public interface PaymentRepository {

    Payment save(Payment payment);

    List<Payment> findByUserId(Long userId);

    Payment findById(Long paymentId);

    List<Payment> findReadyOlderThan(Duration duration);
}
