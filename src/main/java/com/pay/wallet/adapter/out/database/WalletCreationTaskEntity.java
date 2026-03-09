package com.pay.wallet.adapter.out.database;

import com.pay.global.entity.BaseTimeEntity;
import com.pay.wallet.domain.WalletCreationTask;
import com.pay.wallet.domain.WalletCreationTaskStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name= "wallet_creation_task")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletCreationTaskEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletCreationTaskStatus status;

    public static WalletCreationTaskEntity fromDomain(WalletCreationTask task) {
        WalletCreationTaskEntity entity = new WalletCreationTaskEntity();
        entity.taskId = task.getTaskId();
        entity.userId = task.getUserId();
        entity.status = task.getStatus();
        return entity;
    }

    public WalletCreationTask toDomain() {
        return WalletCreationTask.restore(
                this.taskId,
                this.userId,
                this.status,
                this.getCreatedAt(),
                this.getUpdatedAt()
        );
    }
}
