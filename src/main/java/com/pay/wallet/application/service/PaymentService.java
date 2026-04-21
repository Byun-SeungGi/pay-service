package com.pay.wallet.application.service;

import com.pay.wallet.application.port.in.*;
import com.pay.wallet.application.port.out.PaymentRepository;
import com.pay.wallet.application.port.out.TossPaymentPort;
import com.pay.wallet.domain.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService implements PaymentConfirmUseCase, PaymentCancelUseCase {

    private final PaymentRepository paymentRepository;
    private final TossPaymentPort tossPaymentPort;
    private final WalletChargeUseCase walletChargeUseCase;
    private final WalletRefundUseCase walletRefundUseCase;
    private final WalletRefundQueryUseCase walletRefundQueryUseCase;
    private final TransactionTemplate transactionTemplate;

    @Override
    public Payment confirm(String paymentKey, String orderId, Long userId, BigDecimal amount) {

        Payment savedPayment = transactionTemplate.execute(status -> {
            Payment payment = Payment.create(paymentKey, orderId, userId, amount);
            return paymentRepository.save(payment);
        });

        String tossResponse;
        try {
            tossResponse = tossPaymentPort.confirm(paymentKey, orderId, amount);
        } catch (Exception e) {
            transactionTemplate.executeWithoutResult(status ->
                paymentRepository.save(savedPayment.fail())
            );
            throw new IllegalStateException("토스 결제 승인에 실패했습니다.", e);
        }

        return transactionTemplate.execute(status -> {
            Payment approvedPayment = savedPayment.approve().withTossResponse(tossResponse);
            paymentRepository.save(approvedPayment);
            walletChargeUseCase.charge(userId, amount, orderId);
            return approvedPayment;
        });
    }

    @Override
    public Payment cancel(Long paymentId, BigDecimal cancelAmount, Long userId, String idempotencyKey, String cancelReason) {

        Payment payment = paymentRepository.findById(paymentId);

        if(!payment.getUserId().equals(userId)) {
            throw new IllegalStateException("본인 결제만 취소가 가능합니다.");
        }

        if(walletRefundQueryUseCase.isAlreadyRefunded(userId, idempotencyKey)) {
            log.info("중복 결제 취소 요청 - idempotencyKey : {}, userId : {}, paymentId : {}", idempotencyKey, userId, paymentId);

            return payment;
        }

        BigDecimal actualCancelAmount = cancelAmount != null ? cancelAmount : payment.getAmount();

        String tossResponse;
        try {
            tossResponse = tossPaymentPort.cancel(payment.getPaymentKey(), cancelReason, actualCancelAmount, idempotencyKey);
        } catch (Exception e) {
            throw new IllegalStateException("토스 결제 취소에 실패했습니다.", e);
        }

        return transactionTemplate.execute(status -> {
            Payment canceledPayment = actualCancelAmount.compareTo(payment.getAmount()) == 0
                    ? payment.cancel()
                    : payment.cancelPartially(actualCancelAmount);

            walletRefundUseCase.refund(userId, actualCancelAmount, idempotencyKey);

            return paymentRepository.save(canceledPayment);
        });
    }
}
