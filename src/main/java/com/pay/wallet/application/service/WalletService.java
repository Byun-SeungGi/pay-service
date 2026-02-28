package com.pay.wallet.application.service;

import com.pay.wallet.application.port.in.WalletCreateUseCase;
import com.pay.wallet.application.port.out.WalletRepository;
import com.pay.wallet.domain.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService implements WalletCreateUseCase {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public void createWallet(Long userId) {

        if(walletRepository.existsByUserId(userId)) {
            throw new IllegalStateException("이미 지갑이 생성되었습니다.");
        }

        Wallet wallet = Wallet.create(userId);

        walletRepository.save(wallet);
    }


}
