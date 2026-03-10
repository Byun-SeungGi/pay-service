package com.pay.wallet.adapter.in.scheduler;

import com.pay.user.application.port.out.UserCreatedEventOutboxRepository;
import com.pay.user.domain.UserCreatedEventOutbox;
import com.pay.user.domain.UserCreatedEventStatus;
import com.pay.wallet.application.port.in.WalletCreateUseCase;
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
    private UserCreatedEventOutboxRepository userCreatedEventOutboxRepository;

    @Test
    @DisplayName("보상 로직 - 지갑 생성 성공 시 상태값 COMPLETED로 변경")
    void retry_success() {
        UserCreatedEventOutbox event = UserCreatedEventOutbox.pending(1L);
        given(userCreatedEventOutboxRepository.findByStatus(UserCreatedEventStatus.PENDING))
                .willReturn(List.of(event));

        walletCreationScheduler.retryFailWalletCreations();

        verify(walletCreateUseCase, times(1)).createWallet(1L);
        verify(userCreatedEventOutboxRepository, times(1)).save(any(UserCreatedEventOutbox.class));
    }

    @Test
    @DisplayName("보상 로직 - 지갑 생성 실패 시 PENDING 유지")
    void retry_fail() {
        UserCreatedEventOutbox event = UserCreatedEventOutbox.pending(1L);
        given(userCreatedEventOutboxRepository.findByStatus(UserCreatedEventStatus.PENDING))
                .willReturn(List.of(event));
        doThrow(new RuntimeException("DB 오류")).when(walletCreateUseCase).createWallet(1L);

        walletCreationScheduler.retryFailWalletCreations();

        verify(walletCreateUseCase, times(1)).createWallet(1L);
        verify(userCreatedEventOutboxRepository, never()).save(any(UserCreatedEventOutbox.class));

    }
}
