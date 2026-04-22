package com.pay.wallet.adapter.out.tosspayments;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pay.wallet.application.port.out.TossPaymentPort;
import com.pay.wallet.application.port.out.TossPaymentStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Base64;

@Component
public class TossPaymentsAdapter implements TossPaymentPort {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String secretKey;

    public TossPaymentsAdapter(RestClient restClient, ObjectMapper objectMapper, @Value("${tosspayments.secret-key}") String secretKey) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.secretKey = secretKey;
    }

    private record TossConfirmRequest(
            String paymentKey, String orderId, BigDecimal amount
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record TossCancelRequest(
            String cancelReason, BigDecimal cancelAmount
    ) {}

    @Override
    public String confirm(String paymentKey, String orderId, BigDecimal amount) {
        String auth = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes());

        TossConfirmRequest body = new TossConfirmRequest(paymentKey, orderId, amount);

        return restClient.post()
                .uri("https://api.tosspayments.com/v1/payments/confirm")
                .header("Authorization", "Basic " + auth)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);
    }

    @Override
    public String cancel(String paymentKey, String cancelReason, BigDecimal cancelAmount, String idempotencyKey) {
        String auth = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes());

        TossCancelRequest body = new TossCancelRequest(cancelReason, cancelAmount);

        return restClient.post()
                .uri("https://api.tosspayments.com/v1/payments/{paymentKey}/cancel", paymentKey)
                .header("Authorization", "Basic " + auth)
                .header("Idempotency-key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);
    }

    @Override
    public TossPaymentStatus getPayment(String paymentKey) {
        String auth = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes());

        try {
            String body = restClient.get()
                    .uri("https://api.tosspayments.com/v1/payments/{paymentKey}", paymentKey)
                    .header("Authorization", "Basic " + auth)
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
