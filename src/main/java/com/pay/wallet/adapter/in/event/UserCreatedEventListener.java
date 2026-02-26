package com.pay.wallet.adapter.in.event;

import com.pay.user.domain.event.UserCreatedEvent;
import com.pay.wallet.application.port.in.WalletCreateUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCreatedEventListener {

    private final WalletCreateUseCase walletCreateUseCase;

    @EventListener
    public void handleUserCreatedEvent(UserCreatedEvent event) {

        walletCreateUseCase.createWallet(event.userId());
    }
}
