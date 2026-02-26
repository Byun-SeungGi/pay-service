package com.pay.wallet.adapter.out.database;

import com.pay.global.entity.BaseTimeEntity;
import com.pay.wallet.domain.Wallet;
import com.pay.wallet.domain.WalletStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Table(name = "wallet")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Long walletId;

    @Column(name = "external_id", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    private UUID externalId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "balance", precision = 19, scale = 4, nullable = false)
    private BigDecimal balance;

    @Column(name = "version", nullable = false)
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletStatus status;

    public static WalletEntity fromDomain(Wallet wallet) {
        WalletEntity entity = new WalletEntity();
        entity.walletId = wallet.getWalletId();
        entity.externalId = wallet.getExternalId();
        entity.balance = wallet.getBalance();
        entity.version = wallet.getVersion();
        entity.status = wallet.getStatus();
        return entity;
    }

    public Wallet toDomain() {
        return Wallet.restore(
                this.walletId,
                this.externalId,
                this.userId,
                this.balance,
                this.version,
                this.status
        );
    }
}

