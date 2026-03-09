package com.pay.wallet.application.port.out;

import com.pay.wallet.domain.WalletCreationTask;
import com.pay.wallet.domain.WalletCreationTaskStatus;

import java.util.List;

public interface WalletCreationTaskRepository {

    WalletCreationTask save(WalletCreationTask task);

    List<WalletCreationTask> findByStatus(WalletCreationTaskStatus status);
}
