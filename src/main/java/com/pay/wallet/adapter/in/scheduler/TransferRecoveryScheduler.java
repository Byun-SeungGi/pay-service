package com.pay.wallet.adapter.in.scheduler;

import com.pay.wallet.application.port.out.TransferRepository;
import com.pay.wallet.application.service.TransferService;
import com.pay.wallet.domain.Transfer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferRecoveryScheduler {

    private final TransferRepository transferRepository;
    private final TransferService transferService;

    @Scheduled(fixedDelay = 60000)
    public void recoveryStuckTransfers() {
        List<Transfer> stuckTransfers = transferRepository.findStuckWithdrawnTransfer(LocalDateTime.now().minusMinutes(5));

        for(Transfer transfer : stuckTransfers) {
            log.warn("장애 복구 - 환불 처리 시작 - transferId: {}", transfer.getTransferId());
            transferService.refund(transfer.getTransferId());
        }
    }
}
