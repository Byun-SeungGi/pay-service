package com.pay.wallet.application.service;

import com.pay.wallet.application.port.in.*;
import com.pay.wallet.application.port.out.PaymentRepository;
import com.pay.wallet.application.port.out.TossPaymentPort;
import com.pay.wallet.domain.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService implements PaymentConfirmUseCase, PaymentCancelUseCase {

    private final PaymentRepository paymentRepository;
    private final TossPaymentPort tossPaymentPort;
    private final WalletChargeUseCase walletChargeUseCase;
    private final WalletRefundUseCase walletRefundUseCase;
    private final WalletRefundQueryUseCase walletRefundQueryUseCase;

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

    @Override
    @Transactional
    public Payment cancel(Long paymentId, BigDecimal cancelAmount, Long userId, String idempotencyKey, String cancelReason) {

        Payment payment = paymentRepository.findById(paymentId);

        if(!payment.getUserId().equals(userId)) {
            throw new IllegalStateException("본인 결제만 취소가 가능합니다.");
        }

        if(walletRefundQueryUseCase.isAlreadyRefunded(userId, idempotencyKey)) {
            log.info("중복 결제 취소 요청 - idempotencyKey : {}, userId : {}, paymentId : {}", idempotencyKey, userId, paymentId);

            return payment;
        }

        String tossResponse;
        try {
            tossResponse = tossPaymentPort.cancel(payment.getPaymentKey(), cancelReason, cancelAmount, idempotencyKey);
        } catch (Exception e) {
            throw new IllegalStateException("토스 결제 취소에 실패했습니다.", e);
        }

        Payment canceledPayment;
        BigDecimal refundAmount;

        if(cancelAmount == null) {
            canceledPayment = payment.cancel();
            refundAmount = payment.getAmount();
        } else {
            canceledPayment = payment.cancelPartially(cancelAmount);
            refundAmount = cancelAmount;
        }

        walletRefundUseCase.refund(userId, refundAmount, idempotencyKey);

        return paymentRepository.save(canceledPayment);
    }
}
