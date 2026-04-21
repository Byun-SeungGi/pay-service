package com.pay.wallet.application.service;

import com.pay.wallet.application.port.in.WalletRefundUseCase;
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
public class WalletRefundService implements WalletRefundUseCase {

    private final WalletRepository walletRepository;
    private final LedgerRepository ledgerRepository;

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    @Override
    @Transactional
    public Wallet refund(Long userId, BigDecimal amount, String transactionId) {

        String namespacedId = userId + "::" + transactionId;

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("지갑이 존재하지 않습니다."));

        if (ledgerRepository.existsByTransactionId(namespacedId)) {
            log.info("중복 환불 요청 감지 - transactionId : {}, userId : {}", transactionId, userId);
            return wallet;
        }

        Wallet refundWallet = wallet.withDraw(amount);
        Ledger ledger = Ledger.create(
                refundWallet.getWalletId(),
                namespacedId,
                ReferenceType.REFUND,
                refundWallet.getWalletId(),
                TransactionType.DEBIT,
                amount,
                refundWallet.getBalance(),
                "결제 취소"
        );

        Wallet savedWallet = walletRepository.save(refundWallet);

        ledgerRepository.save(ledger);

        return savedWallet;
    }
}
