package com.pay.transfer.application.port.out;

import com.pay.transfer.domain.Transfer;

import java.util.Optional;

public interface TransferRepository {

    Transfer save(Transfer transfer);

    Optional<Transfer> findByTransactionId(String transactionId);
}
