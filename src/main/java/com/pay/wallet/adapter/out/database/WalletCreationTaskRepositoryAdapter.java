package com.pay.wallet.adapter.out.database;

import com.pay.wallet.application.port.out.WalletCreationTaskRepository;
import com.pay.wallet.domain.WalletCreationTask;
import com.pay.wallet.domain.WalletCreationTaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WalletCreationTaskRepositoryAdapter implements WalletCreationTaskRepository {

    private final WalletCreationTaskJpaRepository walletCreationTaskJpaRepository;

    @Override
    public WalletCreationTask save(WalletCreationTask task) {
        WalletCreationTaskEntity entity = WalletCreationTaskEntity.fromDomain(task);
        WalletCreationTaskEntity saved = walletCreationTaskJpaRepository.save(entity);

        return saved.toDomain();
    }

    @Override
    public List<WalletCreationTask> findByStatus(WalletCreationTaskStatus status) {
        return walletCreationTaskJpaRepository.findByStatus(status)
                .stream()
                .map(WalletCreationTaskEntity::toDomain)
                .toList();
    }
}
