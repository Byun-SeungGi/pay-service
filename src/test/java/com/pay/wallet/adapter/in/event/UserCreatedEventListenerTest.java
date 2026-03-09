package com.pay.wallet.adapter.in.event;

import com.pay.user.domain.event.UserCreatedEvent;
import com.pay.wallet.application.port.in.WalletCreateUseCase;
import com.pay.wallet.application.port.out.WalletCreationTaskRepository;
import com.pay.wallet.domain.WalletCreationTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCreatedEventListenerTest {

    @InjectMocks
    private UserCreatedEventListener userCreatedEventListener;

    @Mock
    private WalletCreateUseCase walletCreateUseCase;

    @Mock
    private WalletCreationTaskRepository walletCreationTaskRepository;

    @Test
    @DisplayName("지갑 생성 성공 시 실패 태스크 저장하지 않음")
    void handleUserCreatedEvent_success() {
        UserCreatedEvent event = new UserCreatedEvent(1L);

        userCreatedEventListener.handleUserCreatedEvent(event);

        verify(walletCreateUseCase, times(1)).createWallet(1L);
        verify(walletCreationTaskRepository, never()).save(any(WalletCreationTask.class));
    }

    @Test
    @DisplayName("지갑 생성 실패 시 FAILED 태스크 저장")
    void handleUserCreatedEvent_fail() {
        UserCreatedEvent event = new UserCreatedEvent(1L);
        doThrow(new RuntimeException("DB 오류")).when(walletCreateUseCase).createWallet(1L);

        userCreatedEventListener.handleUserCreatedEvent(event);

        verify(walletCreateUseCase, times(1)).createWallet(1L);
        verify(walletCreationTaskRepository, times(1)).save(any(WalletCreationTask.class));
    }
}
