package com.pay.wallet.application.service;

import com.pay.wallet.application.port.in.WalletRefundQueryUseCase;
import com.pay.wallet.application.port.out.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletRefundQueryService implements WalletRefundQueryUseCase {

    private final LedgerRepository ledgerRepository;

    @Override
    @Transactional
    public boolean isAlreadyRefunded(Long userId, String idempotencyKey) {

        String namespacedId = userId + "::" + idempotencyKey;

        return ledgerRepository.existsByTransactionId(namespacedId);
    }
}
