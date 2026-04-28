package com.pay.wallet.adapter.in.scheduler;

import com.pay.wallet.application.port.in.PaymentReconcileUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReconciliationScheduler {

    private final PaymentReconcileUseCase paymentReconcileUseCase;

    @Scheduled(fixedDelay = 5 * 60 * 1000)  // 5분마다
    public void reconcilePayments() {
        paymentReconcileUseCase.reconcile();
    }
}
