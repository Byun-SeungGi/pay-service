package com.pay.wallet.application.port.in;

import com.pay.wallet.domain.Payment;

import java.util.List;

public interface PaymentFindUseCase {

    List<Payment> findByUserId(Long userId);
}
