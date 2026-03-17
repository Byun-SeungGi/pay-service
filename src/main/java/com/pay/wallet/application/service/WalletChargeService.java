package com.pay.wallet.application.service;

import com.pay.wallet.application.port.in.WalletChargeUseCase;
import com.pay.wallet.application.port.out.LedgerRepository;
import com.pay.wallet.application.port.out.WalletRepository;
import com.pay.wallet.domain.Ledger;
import com.pay.wallet.domain.ReferenceType;
import com.pay.wallet.domain.TransactionType;
import com.pay.wallet.domain.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletChargeService implements WalletChargeUseCase {

    private final WalletRepository walletRepository;
    private final LedgerRepository ledgerRepository;

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    @Override
    @Transactional
    public Wallet charge(Long userId, BigDecimal amount, String transactionId) {



        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("지갑이 존재하지 않습니다."));

        if(ledgerRepository.existsByTransactionId(transactionId)) {
            log.info("중복 충전 요청 감지 - transactionId : {}, userId : {}", transactionId, userId);
            return wallet;
        }

        Wallet chargedWallet = wallet.charge(amount);
        Ledger ledger = Ledger.create(chargedWallet.getWalletId(), transactionId, ReferenceType.CHARGE,
                chargedWallet.getWalletId(), TransactionType.CREDIT, amount, chargedWallet.getBalance(), "충전"
        );
        Wallet savedWallet = walletRepository.save(chargedWallet);

        ledgerRepository.save(ledger);

        return savedWallet;
    }
}
