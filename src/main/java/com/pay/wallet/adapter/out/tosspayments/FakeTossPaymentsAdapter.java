package com.pay.wallet.adapter.out.tosspayments;

import com.pay.wallet.application.port.out.TossCancelResult;
import com.pay.wallet.application.port.out.TossConfirmResult;
import com.pay.wallet.application.port.out.TossPaymentPort;
import com.pay.wallet.application.port.out.TossPaymentStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@Profile("loadtest")
public class FakeTossPaymentsAdapter implements TossPaymentPort {

    @PostConstruct
    public void logConfig() {
        log.info(">>> FakeTossPaymentsAdapter loaded: failureRate={}, minLatencyMs={}, maxLatencyMs={}",
                failureRate, minLatencyMs, maxLatencyMs);
    }

    private final double failureRate;
    private final long minLatencyMs;
    private final long maxLatencyMs;

    public FakeTossPaymentsAdapter(
            @Value("${fake-toss.failure-rate:0.05}") double failureRate,
            @Value("${fake-toss.min-latency-ms:30}") long minLatencyMs,
            @Value("${fake-toss.max-latency-ms:150}") long maxLatencyMs
    ) {
        this.failureRate = failureRate;
        this.minLatencyMs = minLatencyMs;
        this.maxLatencyMs = maxLatencyMs;
    }

    @Override
    @CircuitBreaker(name = "toss")
    @Retry(name = "toss")
    public TossConfirmResult confirm(String paymentKey, String orderId, BigDecimal amount) {
        simulateLatency();
        simulateFailure();

        String rawResponse = """
                    {
                      "paymentKey": "%s",
                      "orderId": "%s",
                      "totalAmount": %s,
                      "status": "DONE",
                      "method": "카드"
                    }
                    """.formatted(paymentKey, orderId, amount);

        return new TossConfirmResult(paymentKey, rawResponse);
    }

    @Override
    @CircuitBreaker(name = "toss")
    @Retry(name = "toss")
    public TossCancelResult cancel(String paymentKey, String cancelReason, BigDecimal cancelAmount, String idempotencyKey) {
        simulateLatency();
        simulateFailure();

        String rawResponse= """
                  {
                    "paymentKey": "%s",
                    "status": "CANCELED"
                  }
                  """.formatted(paymentKey);

        return new TossCancelResult(paymentKey, rawResponse);
    }

    @Override
    public TossPaymentStatus getPayment(String paymentKey) {
        return new TossPaymentStatus(TossPaymentStatus.Action.APPROVE, "{}");
    }

    private void simulateLatency() {
        if (maxLatencyMs <= 0) return;
        long delay = ThreadLocalRandom.current().nextLong(minLatencyMs, maxLatencyMs + 1);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private void simulateFailure() {
        if (failureRate <= 0) return;
        if (ThreadLocalRandom.current().nextDouble() >= failureRate) return;

        int failureType = ThreadLocalRandom.current().nextInt(3);
        switch (failureType) {
            case 0 -> throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
            case 1 -> throw new HttpServerErrorException(HttpStatus.BAD_GATEWAY);
            case 2 -> throw new ResourceAccessException("Simulated network timeout");
        }
    }
}
