package com.pay.wallet.application.service;

import com.pay.wallet.application.port.in.PaymentReconcileUseCase;
import com.pay.wallet.application.port.in.WalletChargeUseCase;
import com.pay.wallet.application.port.out.PaymentRepository;
import com.pay.wallet.application.port.out.TossPaymentPort;
import com.pay.wallet.application.port.out.TossPaymentStatus;
import com.pay.wallet.domain.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReconciliationService implements PaymentReconcileUseCase {

    private static final Duration MIN_AGE = Duration.ofMinutes(5);

    private final PaymentRepository paymentRepository;
    private final TossPaymentPort tossPaymentPort;
    private final WalletChargeUseCase walletChargeUseCase;
    private final TransactionTemplate transactionTemplate;

    @Override
    public void reconcile() {
        List<Payment> pendingPayments = paymentRepository.findReadyOlderThan(MIN_AGE);
        log.info("결제 대사 시작 - 대상 건수  : {}", pendingPayments.size());

        for(Payment payment : pendingPayments) {
            reconcileOne(payment);
        }

        log.info("결제 대사 종료");
    }

    private void reconcileOne(Payment payment) {
        try {
            TossPaymentStatus tossStatus = tossPaymentPort.getPayment(payment.getPaymentKey());

            switch(tossStatus.action()) {
                case APPROVE -> approveAndCharge(payment, tossStatus.rawResponse());
                case FAIL -> markFailed(payment, tossStatus.rawResponse());
                case SKIP -> log.info("대사 skip (토스 처리 중) - paymentId: {}", payment.getPaymentId());
            }

        } catch (Exception e) {
            log.error("결제 대사 중 예외 발생 - paymentId: {}", payment.getPaymentId(), e);
        }
    }

    private void approveAndCharge(Payment payment, String rawResponse) {
        transactionTemplate.execute(status -> {
            Payment approved = payment.approve().withTossResponse(rawResponse);
            paymentRepository.save(approved);
            walletChargeUseCase.charge(payment.getUserId(), payment.getAmount(), payment.getOrderId());
            return null;
        });
        log.info("대사 승인 완료 - paymentId: {}, userId: {}", payment.getPaymentId(), payment.getUserId());
    }

    private void markFailed(Payment payment, String rawResponse) {
        transactionTemplate.executeWithoutResult(status -> {
            Payment failed = payment.fail().withTossResponse(rawResponse);
            paymentRepository.save(failed);
        });
        log.info("대사 실패 처리 완료 - paymentId: {}, userId: {}", payment.getPaymentId(), payment.getUserId());
    }
}
