package com.pay.transfer.application.port.out;

import com.pay.wallet.domain.Ledger;

public interface TransferLedgerPort {

    Ledger save(Ledger ledger);
}
