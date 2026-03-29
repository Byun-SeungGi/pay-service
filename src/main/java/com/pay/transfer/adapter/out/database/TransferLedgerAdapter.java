package com.pay.transfer.adapter.out.database;

import com.pay.transfer.application.port.out.TransferLedgerPort;
import com.pay.wallet.adapter.out.database.LedgerEntity;
import com.pay.wallet.adapter.out.database.LedgerJpaRepository;
import com.pay.wallet.domain.Ledger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TransferLedgerAdapter implements TransferLedgerPort {

    private final LedgerJpaRepository ledgerJpaRepository;

    @Override
    public Ledger save(Ledger ledger) {

        LedgerEntity saved = ledgerJpaRepository.save(LedgerEntity.fromDomain(ledger));

        return saved.toDomain();
    }
}
