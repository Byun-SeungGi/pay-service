package com.pay.wallet.adapter.in.scheduler;

import com.pay.user.application.port.out.UserCreatedEventOutboxRepository;
import com.pay.user.domain.UserCreatedEventOutbox;
import com.pay.user.domain.UserCreatedEventStatus;
import com.pay.wallet.application.port.in.WalletCreateUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletCreationScheduler {

    private final WalletCreateUseCase walletCreateUseCase;
    private final UserCreatedEventOutboxRepository userCreatedEventOutboxRepository;

    @Scheduled(fixedDelay = 30000)
    @SchedulerLock(name = "retryFailWalletCreations", lockAtMostFor = "PT29S", lockAtLeastFor = "PT29S")
    public void retryFailWalletCreations() {
        List<UserCreatedEventOutbox> pendingEvents = userCreatedEventOutboxRepository.findByStatus(UserCreatedEventStatus.PENDING);

        for(UserCreatedEventOutbox event : pendingEvents) {
            try {
                walletCreateUseCase.createWallet(event.getUserId());
                event.complete();
                userCreatedEventOutboxRepository.save(event);
                log.info("보상 로직 - 지갑 생성 완료 - userId : {}", event.getUserId());
            } catch (Exception e) {

                log.error("보상 로직 - 지갑 생성 실패 - userId : {}", event.getUserId());
            }
        }
    }
}
