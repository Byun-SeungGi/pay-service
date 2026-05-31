package com.pay.wallet.adapter.out.database;

import com.pay.wallet.domain.Ledger;
import com.pay.wallet.domain.ReferenceType;
import com.pay.wallet.domain.TransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "ledger", indexes = {
        @Index(name = "uk_ledger_transaction_id", columnList = "transaction_id", unique = true),
        @Index(name = "idx_ledger_wallet_date", columnList = "wallet_id, created_at"),
        @Index(name = "idx_ledger_reference", columnList = "reference_type, reference_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ledger_id")
    private Long ledgerId;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ReferenceType referenceType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "balance_snapshot", nullable = false)
    private BigDecimal balanceSnapshot;

    @Column(name = "memo")
    private String memo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public static LedgerEntity fromDomain(Ledger ledger) {
        LedgerEntity entity = new LedgerEntity();
        entity.ledgerId = ledger.getLedgerId();
        entity.walletId = ledger.getWalletId();
        entity.transactionId = ledger.getTransactionId();
        entity.referenceType = ledger.getReferenceType();
        entity.referenceId = ledger.getReferenceId();
        entity.transactionType = ledger.getTransactionType();
        entity.amount = ledger.getAmount();
        entity.balanceSnapshot = ledger.getBalanceSnapshot();
        entity.memo = ledger.getMemo();
        entity.createdAt = ledger.getCreatedAt() != null ? ledger.getCreatedAt() : LocalDateTime.now();
        return entity;
    }

    public Ledger toDomain() {
        return Ledger.restore(
                this.ledgerId,
                this.walletId,
                this.transactionId,
                this.referenceType,
                this.referenceId,
                this.transactionType,
                this.amount,
                this.balanceSnapshot,
                this.memo,
                this.getCreatedAt()
        );
    }
}
