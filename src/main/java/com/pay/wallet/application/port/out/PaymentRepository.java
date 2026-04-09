package com.pay.wallet.application.port.out;

import com.pay.wallet.domain.Payment;

public interface PaymentRepository {

    Payment save(Payment payment);
}
