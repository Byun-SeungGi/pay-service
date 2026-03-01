package com.pay.wallet.adapter.in.event;

import com.pay.global.kafka.KafkaProducer;
import com.pay.global.kafka.KafkaTopics;
import com.pay.user.domain.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreatedEventListener {

    private final KafkaProducer kafkaProducer;

    @Async
    @TransactionalEventListener
    public void handleUserCreatedEvent(UserCreatedEvent event) {

        try {
            kafkaProducer.send(KafkaTopics.WALLET_CREATION, String.valueOf(event.userId()));
        } catch (Exception e) {
            log.error("지갑 생성 실패 - userId: {}", event.userId(), e);
        }

    }
}
