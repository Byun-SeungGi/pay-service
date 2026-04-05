package com.pay.wallet.application.port.out;

import com.pay.wallet.domain.Transfer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransferRepository {

    Transfer save(Transfer transfer);

    Optional<Transfer> findByTransactionId(String transactionId);

    Optional<Transfer> findByIdForUpdate(Long transferId);

    List<Transfer> findStuckWithdrawnTransfer(LocalDateTime before);
}
