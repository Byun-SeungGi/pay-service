package com.pay.wallet.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WalletCreationTask {

    private final Long taskId;
    private final Long userId;
    private WalletCreationTaskStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder(builderMethodName = "innerBuilder", access = AccessLevel.PRIVATE)

    private WalletCreationTask(Long taskId, Long userId, WalletCreationTaskStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.taskId = taskId;
        this.userId = userId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static WalletCreationTask fail(Long userId) {
        return WalletCreationTask.innerBuilder()
                .userId(userId)
                .status(WalletCreationTaskStatus.FAILED)
                .build();
    }

    public static WalletCreationTask restore(Long taskId, Long userId, WalletCreationTaskStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return WalletCreationTask.innerBuilder()
                .taskId(taskId)
                .userId(userId)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public void complete() {
        this.status = WalletCreationTaskStatus.COMPLETED;
    }
}
