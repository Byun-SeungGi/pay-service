package com.pay.wallet.application.service;

import com.pay.wallet.application.port.in.WalletChargeUseCase;
import com.pay.wallet.application.port.out.WalletRepository;
import com.pay.wallet.domain.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletCreateService implements WalletChargeUseCase {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public Wallet charge(Long userId, BigDecimal amount) {

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("지갑이 존재하지 않습니다."));

        Wallet chargedWallet = wallet.charge(amount);

        return walletRepository.save(chargedWallet);
    }
}
