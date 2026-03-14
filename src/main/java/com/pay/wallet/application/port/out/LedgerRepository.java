package com.pay.wallet.application.port.out;

import com.pay.wallet.domain.Ledger;

import java.util.List;

public interface LedgerRepository {

    Ledger save(Ledger ledger);

    List<Ledger> findByWalletId(Long walletId);
}
