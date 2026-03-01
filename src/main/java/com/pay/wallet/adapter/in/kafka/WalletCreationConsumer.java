package com.pay.wallet.adapter.in.kafka;

import com.pay.global.kafka.KafkaTopics;
import com.pay.wallet.application.port.in.WalletCreateUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletCreationConsumer {

    private final WalletCreateUseCase walletCreateUseCase;

    @KafkaListener(topics = KafkaTopics.WALLET_CREATION, groupId = "pay-service")
    public void consume(String message) {
        Long userId = Long.valueOf(message);
        log.info("지갑 생성 메시지 수신 완료 - userId: {}", userId);

        walletCreateUseCase.createWallet(userId);
    }
}
