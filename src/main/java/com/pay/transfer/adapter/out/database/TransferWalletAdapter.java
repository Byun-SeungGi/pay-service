package com.pay.transfer.adapter.out.database;

import com.pay.transfer.application.port.out.TransferWalletPort;
import com.pay.wallet.adapter.out.database.WalletEntity;
import com.pay.wallet.adapter.out.database.WalletJpaRepository;
import com.pay.wallet.domain.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
@RequiredArgsConstructor
public class TransferWalletAdapter implements TransferWalletPort {

    private final WalletJpaRepository walletJpaRepository;

    @Override
    public void withdraw(Long userId, BigDecimal amount) {

        WalletEntity walletEntity = walletJpaRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("지갑이 존재하지 않습니다."));

        Wallet wallet = walletEntity.toDomain();
        Wallet withdrawnWallet = wallet.withDraw(amount);

        walletJpaRepository.save(WalletEntity.fromDomain(wallet));
    }

    @Override
    public void deposit(Long userId, BigDecimal amount) {

    }
}
