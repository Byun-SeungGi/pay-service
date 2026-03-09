package com.pay.wallet.adapter.in.scheduler;

import com.pay.wallet.application.port.in.WalletCreateUseCase;
import com.pay.wallet.application.port.out.WalletCreationTaskRepository;
import com.pay.wallet.domain.WalletCreationTask;
import com.pay.wallet.domain.WalletCreationTaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletCreationSchedulerTest {

    @InjectMocks
    private WalletCreationScheduler walletCreationScheduler;

    @Mock
    private WalletCreateUseCase walletCreateUseCase;

    @Mock
    private WalletCreationTaskRepository walletCreationTaskRepository;

    @Test
    @DisplayName("보상 로직 - 지갑 생성 성공 시 상태값 COMPLETED로 변경")
    void retry_success() {
        WalletCreationTask task = WalletCreationTask.fail(1L);
        given(walletCreationTaskRepository.findByStatus(WalletCreationTaskStatus.FAILED))
                .willReturn(List.of(task));

        walletCreationScheduler.retryFailWalletCreations();

        verify(walletCreateUseCase, times(1)).createWallet(1L);
        verify(walletCreationTaskRepository, times(1)).save(any(WalletCreationTask.class));
    }

    @Test
    @DisplayName("보상 로직 - 지갑 생성 실패 시 FAILED 유지")
    void retry_fail() {
        WalletCreationTask task = WalletCreationTask.fail(1L);
        given(walletCreationTaskRepository.findByStatus(WalletCreationTaskStatus.FAILED))
                .willReturn(List.of(task));
        doThrow(new RuntimeException("DB 오류")).when(walletCreateUseCase).createWallet(1L);

        walletCreationScheduler.retryFailWalletCreations();

        verify(walletCreateUseCase, times(1)).createWallet(1L);
        verify(walletCreationTaskRepository, never()).save(any(WalletCreationTask.class));

    }
}
