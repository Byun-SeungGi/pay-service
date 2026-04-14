package com.pay.wallet.application.service;

import com.pay.wallet.application.port.in.PaymentFindUseCase;
import com.pay.wallet.application.port.out.PaymentRepository;
import com.pay.wallet.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentFindService implements PaymentFindUseCase {

    private final PaymentRepository paymentRepository;

    @Override
    public List<Payment> findByUserId(Long userId) {
        return paymentRepository.findByUserId(userId);
    }
}
