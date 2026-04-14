package com.pay.wallet.adapter.out.tosspayments;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pay.wallet.application.port.out.TossPaymentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Base64;

@Component
public class TossPaymentsAdapter implements TossPaymentPort {

    private final RestClient restClient;
    private final String secretKey;

    public TossPaymentsAdapter(RestClient restClient, @Value("${tosspayments.secret-key}") String secretKey) {
        this.restClient = restClient;
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
}
