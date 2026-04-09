package com.pay.wallet.application.service;

import com.pay.wallet.application.port.in.PaymentConfirmUseCase;
import com.pay.wallet.application.port.in.WalletChargeUseCase;
import com.pay.wallet.application.port.out.PaymentRepository;
import com.pay.wallet.application.port.out.TossPaymentPort;
import com.pay.wallet.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService implements PaymentConfirmUseCase {

    private final PaymentRepository paymentRepository;
    private final TossPaymentPort tossPaymentPort;
    private final WalletChargeUseCase walletChargeUseCase;

    @Override
    @Transactional
    public Payment confirm(String paymentKey, String orderId, Long userId, BigDecimal amount) {

        Payment payment = Payment.create(paymentKey, orderId, userId, amount);
        Payment savedPayment = paymentRepository.save(payment);

        String tossResponse;
        try {
            tossResponse = tossPaymentPort.confirm(paymentKey, orderId, amount);
        } catch (Exception e) {
            paymentRepository.save(savedPayment.fail());
            throw new IllegalStateException("토스 결제 승인에 실패했습니다.", e);
        }

        Payment approvedPayment = savedPayment.approve().withTossResponse(tossResponse);
        paymentRepository.save(approvedPayment);

        walletChargeUseCase.charge(userId, amount, UUID.randomUUID().toString());

        return approvedPayment;
    }
}
