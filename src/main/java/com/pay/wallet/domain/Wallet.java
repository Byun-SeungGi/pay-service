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

    // 지갑 충전
    public Wallet charge(BigDecimal amount) {

        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("충전 금액은 0원보다 커야 합니다.");
        }

        if(this.status != WalletStatus.ACTIVE) {
            throw new IllegalStateException("사용할 수 있는 지갑이 아닙니다.");
        }

        return Wallet.innerBuilder()
                .walletId(this.walletId)
                .externalId(this.externalId)
                .userId(this.userId)
                .balance(this.balance.add(amount))
                .version(this.version)
                .status(this.status)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
