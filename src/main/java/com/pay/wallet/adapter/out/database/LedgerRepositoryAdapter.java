package com.pay.wallet.adapter.out.database;

import com.pay.wallet.application.port.out.LedgerRepository;
import com.pay.wallet.domain.Ledger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LedgerRepositoryAdapter implements LedgerRepository {

    private final LedgerJpaRepository ledgerJpaRepository;

    @Override
    public Ledger save(Ledger ledger) {
        LedgerEntity entity = LedgerEntity.fromDomain(ledger);
        LedgerEntity saved = ledgerJpaRepository.save(entity);

        return saved.toDomain();
    }

    @Override
    public List<Ledger> findByWalletId(Long walletId) {
        return ledgerJpaRepository.findByWalletId(walletId)
                .stream()
                .map(LedgerEntity::toDomain)
                .toList();
    }
}
