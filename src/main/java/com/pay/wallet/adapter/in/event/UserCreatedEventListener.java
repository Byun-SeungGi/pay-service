package com.pay.wallet.adapter.in.event;

import com.pay.user.domain.event.UserCreatedEvent;
import com.pay.wallet.application.port.in.WalletCreateUseCase;
import com.pay.wallet.application.port.out.WalletCreationTaskRepository;
import com.pay.wallet.domain.WalletCreationTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreatedEventListener {

    private final WalletCreateUseCase walletCreateUseCase;
    private final WalletCreationTaskRepository walletCreationTaskRepository;

    @Async
    @TransactionalEventListener
    public void handleUserCreatedEvent(UserCreatedEvent event) {

        try {
            walletCreateUseCase.createWallet(event.userId());
        } catch (Exception e) {
            log.error("지갑 생성 실패 - userId: {}", event.userId(), e);
            walletCreationTaskRepository.save(WalletCreationTask.fail(event.userId()));
            // 보상스케줄러 적용예정
        }

    }
}
