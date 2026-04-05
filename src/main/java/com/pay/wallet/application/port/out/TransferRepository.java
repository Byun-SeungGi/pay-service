package com.pay.wallet.application.port.out;

import com.pay.wallet.domain.Transfer;

import java.util.Optional;

public interface TransferRepository {

    Transfer save(Transfer transfer);

    Optional<Transfer> findByTransactionId(String transactionId);
}
