package com.pay.wallet.adapter.out.database;

import com.pay.wallet.domain.WalletCreationTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletCreationTaskJpaRepository extends JpaRepository<WalletCreationTaskEntity, Long> {

    List<WalletCreationTaskEntity> findByStatus(WalletCreationTaskStatus status);
}
