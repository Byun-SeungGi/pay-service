package com.pay.wallet.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Wallet {

    private final Long walletId;
    private final UUID externalId;
    private final Long userId;
    private final BigDecimal balance;
    private final Long version;
    private final WalletStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder(builderMethodName = "innerBuilder", access = AccessLevel.PRIVATE)
    private Wallet(Long walletId, UUID externalId, Long userId, BigDecimal balance, Long version, WalletStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.walletId = walletId;
        this.externalId = externalId;
        this.userId = userId;
        this.balance = balance;
        this.version = version;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Wallet create(Long userId) {
        return Wallet.innerBuilder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .version(0L)
                .externalId(UUID.randomUUID())
                .status(WalletStatus.ACTIVE)
                .build();
    }

    public static Wallet restore(Long walletId, UUID externalId, Long userId, BigDecimal balance, Long version, WalletStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return Wallet.innerBuilder()
                .walletId(walletId)
                .externalId(externalId)
                .userId(userId)
                .balance(balance)
                .version(version)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
