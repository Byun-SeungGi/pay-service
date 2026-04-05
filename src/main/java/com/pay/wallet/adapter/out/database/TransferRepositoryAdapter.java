package com.pay.wallet.adapter.out.database;

import com.pay.wallet.application.port.out.TransferRepository;
import com.pay.wallet.domain.Transfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransferRepositoryAdapter implements TransferRepository {

    private final TransferJpaRepository transferJpaRepository;

    @Override
    public Transfer save(Transfer transfer) {
        TransferEntity entity = TransferEntity.fromDomain(transfer);
        TransferEntity saved = transferJpaRepository.save(entity);

        return saved.toDomain();
    }

    @Override
    public Optional<Transfer> findByTransactionId(String transactionId) {
        return transferJpaRepository.findByTransactionId(transactionId)
                .map(TransferEntity::toDomain);
    }

    @Override
    public Optional<Transfer> findByIdForUpdate(Long transferId) {
        return transferJpaRepository.findByIdForUpdate(transferId)
                .map(TransferEntity::toDomain);
    }

    @Override
    public List<Transfer> findStuckWithdrawnTransfer(LocalDateTime before) {
        return transferJpaRepository.findStuckWithdrawnTransfer(before)
                .stream()
                .map(TransferEntity::toDomain)
                .toList();

    }


}
