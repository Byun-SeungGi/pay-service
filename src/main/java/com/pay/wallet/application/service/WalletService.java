package com.pay.wallet.application.service;

import com.pay.wallet.application.port.in.WalletCreateUseCase;
import com.pay.wallet.application.port.out.WalletRepository;
import com.pay.wallet.domain.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService implements WalletCreateUseCase {

    private final WalletRepository walletRepository;

    @Override
    public void createWallet(Long userId) {

        Wallet wallet = Wallet.create(userId);

        walletRepository.save(wallet);
    }
}
