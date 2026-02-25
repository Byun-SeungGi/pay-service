package com.pay.wallet.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Wallet {

    private Long walletId;
    private UUID externalId;
    private Long userId;
    private float balance;
    private Long version;
    private WalletStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder(builderMethodName = "innerBuilder")
    public Wallet(Long walletId, UUID externalId, Long userId, float balance, Long version, WalletStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.walletId = walletId;
        this.externalId = externalId;
        this.userId = userId;
        this.balance = balance;
        this.version = version;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Wallet create(Long userId, float balance, Long version, WalletStatus status) {
        return Wallet.innerBuilder()
                .userId(userId)
                .balance(0L)
                .version(0L)
                .externalId(UUID.randomUUID())
                .status(WalletStatus.ACTIVE)
                .build();
    }
}
