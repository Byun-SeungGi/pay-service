package com.pay.wallet.application.service;

import com.pay.wallet.application.port.in.*;
import com.pay.wallet.application.port.out.PaymentRepository;
import com.pay.wallet.application.port.out.TossCancelResult;
import com.pay.wallet.application.port.out.TossConfirmResult;
import com.pay.wallet.application.port.out.TossPaymentPort;
import com.pay.wallet.domain.Payment;
import com.pay.wallet.domain.PaymentStatus;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final MeterRegistry meterRegistry;

    private Counter paymentSuccessCounter;
    private Counter paymentFailureCounter;
    private Timer paymentConfirmTimer;
    private Timer tossApiTimer;
    private Map<PaymentStatus, Counter> statusCounters;

    @PostConstruct
    private void initMetrics() {
        this.paymentSuccessCounter = Counter.builder("payment.confirm.count")
                .tag("result", "success")
                .description("결제 승인 횟수 (성공/실패)")
                .register(meterRegistry);

        this.paymentFailureCounter = Counter.builder("payment.confirm.count")
                .tag("result", "failure")
                .description("결제 승인 횟수 (성공/실패)")
                .register(meterRegistry);

        this.paymentConfirmTimer = Timer.builder("payment.confirm.duration")
                .description("결제 승인 전체 처리 시간")
                .register(meterRegistry);

        this.tossApiTimer = Timer.builder("payment.toss.api.duration")
                .tag("operation", "confirm")
                .description("Toss API 호출시간")
                .register(meterRegistry);
        this.statusCounters = Arrays.stream(PaymentStatus.values())
                .collect(Collectors.toMap(
                        status -> status,
                        status -> Counter.builder("payment.status.count")
                                .tag("status", status.name())
                                .description("결제 상태 전이 횟수")
                                .register(meterRegistry)
                ));
    }

    @Override
    public Payment confirm(String paymentKey, String orderId, Long userId, BigDecimal amount) {
        return paymentConfirmTimer.record(() -> {
            try {
                Payment savedPayment = transactionTemplate.execute(status -> {
                    Payment payment = Payment.create(paymentKey, orderId, userId, amount);
                    return paymentRepository.save(payment);
                });
                statusCounters.get(PaymentStatus.READY).increment();

                TossConfirmResult confirmResult;
                try {
                    confirmResult = tossApiTimer.recordCallable(() -> tossPaymentPort.confirm(paymentKey, orderId, amount));
                } catch (CallNotPermittedException e) {
                    statusCounters.get(PaymentStatus.FAILED).increment();
                    throw e;
                } catch (Exception e) {
                    transactionTemplate.executeWithoutResult(status ->
                            paymentRepository.save(savedPayment.fail())
                    );
                    statusCounters.get(PaymentStatus.FAILED).increment();
                    throw new IllegalStateException("토스 결제 승인에 실패했습니다.", e);
                }

                walletChargeUseCase.charge(userId, amount, orderId);

                Payment result =  transactionTemplate.execute(status -> {
                    Payment approvedPayment = savedPayment.approve().withTossResponse(confirmResult.rawResponse());
                    return  paymentRepository.save(approvedPayment);
                });
                statusCounters.get(PaymentStatus.APPROVED).increment();

                paymentSuccessCounter.increment();
                return result;
            } catch (RuntimeException e) {
                paymentFailureCounter.increment();
                throw e;
            }
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

        TossCancelResult cancelResult;
        try {
            cancelResult = tossPaymentPort.cancel(payment.getPaymentKey(), cancelReason, actualCancelAmount, idempotencyKey);
        } catch (CallNotPermittedException e) {
          throw e;
        } catch (Exception e) {
            throw new IllegalStateException("토스 결제 취소에 실패했습니다.", e);
        }

        Payment saved = transactionTemplate.execute(status -> {
            Payment canceledPayment = actualCancelAmount.compareTo(payment.getAmount()) == 0
                    ? payment.cancel()
                    : payment.cancelPartially(actualCancelAmount);

            walletRefundUseCase.refund(userId, actualCancelAmount, idempotencyKey);

            return paymentRepository.save(canceledPayment);
        });
        statusCounters.get(saved.getStatus()).increment();
        return saved;
    }
}
