package com.pay.wallet.adapter.in.event;

import com.pay.user.application.port.out.UserCreatedEventOutboxRepository;
import com.pay.user.domain.event.UserCreatedEvent;
import com.pay.wallet.application.port.in.WalletCreateUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletCreationEventListener {

    private final WalletCreateUseCase walletCreateUseCase;
    private final UserCreatedEventOutboxRepository userCreatedEventOutboxRepository;

    @Async
    @TransactionalEventListener
    public void handleUserCreatedEvent(UserCreatedEvent event) {

        try {
            walletCreateUseCase.createWallet(event.userId());
            userCreatedEventOutboxRepository.findByUserId(event.userId())
                    .ifPresent(task -> {
                        task.complete();
                        userCreatedEventOutboxRepository.save(task);
                    });
        } catch (Exception e) {
            log.error("지갑 생성 실패 - userId: {}", event.userId(), e);
        }

    }
}
