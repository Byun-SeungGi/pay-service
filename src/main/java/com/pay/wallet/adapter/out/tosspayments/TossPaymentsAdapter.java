package com.pay.wallet.adapter.out.tosspayments;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pay.wallet.application.port.out.TossCancelResult;
import com.pay.wallet.application.port.out.TossConfirmResult;
import com.pay.wallet.application.port.out.TossPaymentPort;
import com.pay.wallet.application.port.out.TossPaymentStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
@Profile("!loadtest")
public class TossPaymentsAdapter implements TossPaymentPort {

    private final ObjectMapper objectMapper;
    private final RestClient tossRestClient;

    public TossPaymentsAdapter(@Qualifier("tossRestClient") RestClient tossRestClient, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.tossRestClient = tossRestClient;
    }

    private record TossConfirmRequest(
            String paymentKey, String orderId, BigDecimal amount
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record TossCancelRequest(
            String cancelReason, BigDecimal cancelAmount
    ) {}

    @Override
    @CircuitBreaker(name ="toss")
    @Retry(name ="toss")
    public TossConfirmResult confirm(String paymentKey, String orderId, BigDecimal amount) {
        TossConfirmRequest body = new TossConfirmRequest(paymentKey, orderId, amount);

        String rawResponse = tossRestClient.post()
                .uri("/confirm")
                .body(body)
                .retrieve()
                .body(String.class);

        return new TossConfirmResult(paymentKey, rawResponse);
    }

    @Override
    @CircuitBreaker(name ="toss")
    @Retry(name ="toss")
    public TossCancelResult cancel(String paymentKey, String cancelReason, BigDecimal cancelAmount, String idempotencyKey) {

        TossCancelRequest body = new TossCancelRequest(cancelReason, cancelAmount);

        String rawResponse = tossRestClient.post()
                .uri("/{paymentKey}/cancel", paymentKey)
                .header("Idempotency-key", idempotencyKey)
                .body(body)
                .retrieve()
                .body(String.class);

        return new TossCancelResult(paymentKey, rawResponse);
    }

    @Override
    public TossPaymentStatus getPayment(String paymentKey) {
        try {
            String body = tossRestClient.get()
                    .uri("/{paymentKey}", paymentKey)
                    .retrieve()
                    .body(String.class);

            String tossStatus = objectMapper.readTree(body).path("status").asText();
            return new TossPaymentStatus(mapToAction(tossStatus), body);
        } catch(HttpClientErrorException.NotFound e) {
            // 토스 결제건이 아예 없을경우
            return new TossPaymentStatus(TossPaymentStatus.Action.FAIL, e.getResponseBodyAsString());
        } catch (Exception e) {
            // 여러가지 오류(네트워크, json 파싱 실패 등)
            return new TossPaymentStatus(TossPaymentStatus.Action.SKIP, "");
        }
    }

    private TossPaymentStatus.Action mapToAction(String tossStatus) {
        return switch (tossStatus) {
            case "DONE" -> TossPaymentStatus.Action.APPROVE;
            case "ABORTED", "EXPIRED", "CANCELED", "PARTIAL_CANCELED"
                    -> TossPaymentStatus.Action.FAIL;
            case "READY", "IN_PROGRESS", "WAITING_FOR_DEPOSIT"
                    -> TossPaymentStatus.Action.SKIP;
            default -> TossPaymentStatus.Action.SKIP; // 알 수 없는 상태 → skip
        };
    }
}
