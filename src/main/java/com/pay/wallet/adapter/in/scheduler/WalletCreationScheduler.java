package com.pay.wallet.adapter.in.scheduler;

import com.pay.wallet.application.port.in.WalletCreateUseCase;
import com.pay.wallet.application.port.out.WalletCreationTaskRepository;
import com.pay.wallet.domain.WalletCreationTask;
import com.pay.wallet.domain.WalletCreationTaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletCreationScheduler {

    private final WalletCreateUseCase walletCreateUseCase;
    private final WalletCreationTaskRepository walletCreationTaskRepository;

    @Scheduled(fixedDelay = 30000)
    public void retryFailWalletCreations() {
        List<WalletCreationTask> failedTasks = walletCreationTaskRepository.findByStatus(WalletCreationTaskStatus.FAILED);

        for(WalletCreationTask task : failedTasks) {
            try {
                walletCreateUseCase.createWallet(task.getUserId());
                task.complete();
                walletCreationTaskRepository.save(task);
                log.info("보상 로직 - 지갑 생성 완료 - userId : {}", task.getUserId());
            } catch (Exception e) {
                log.error("보상 로직 - 지갑 생성 실패 - userId : {}", task.getUserId());
            }
        }
    }


}
