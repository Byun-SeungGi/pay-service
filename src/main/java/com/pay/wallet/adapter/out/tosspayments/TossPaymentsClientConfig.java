package com.pay.wallet.adapter.out.tosspayments;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class TossPaymentsClientConfig {

    @Bean
    public RestClient tossRestClient(RestClient.Builder builder, @Value("${tosspayments.secret-key}") String secretKey) {

        String auth = "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        return builder
                .baseUrl("https://api.tosspayments.com/v1/payments")
                .defaultHeader("Authorization", auth)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
