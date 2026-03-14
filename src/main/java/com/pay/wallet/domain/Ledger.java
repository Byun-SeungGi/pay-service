package com.pay.wallet.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class Ledger {

    private final Long ledgerId;
    private final Long walletId;
    private final String transactionId;
    private final ReferenceType referenceType;
    private final Long referenceId;
    private final TransactionType transactionType;
    private final BigDecimal amount;
    private final BigDecimal balanceSnapshot;
    private final String memo;
    private final LocalDateTime createdAt;

    @Builder(builderMethodName = "innerBuilder", access = AccessLevel.PRIVATE)
    private Ledger(Long ledgerId, Long walletId, String transactionId, ReferenceType referenceType, Long referenceId, TransactionType transactionType, BigDecimal amount, BigDecimal balanceSnapshot, String memo, LocalDateTime createdAt) {
        this.ledgerId = ledgerId;
        this.walletId = walletId;
        this.transactionId = transactionId;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceSnapshot = balanceSnapshot;
        this.memo = memo;
        this.createdAt = createdAt;
    }

    public static Ledger create(Long walletId, String transactionId, ReferenceType referenceType, Long referenceId, TransactionType transactionType, BigDecimal amount, BigDecimal balanceSnapshot, String memo) {

        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야합니다.");
        }

        return Ledger.innerBuilder()
                .walletId(walletId)
                .transactionId(transactionId)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .transactionType(transactionType)
                .amount(amount)
                .balanceSnapshot(balanceSnapshot)
                .memo(memo)
                .build();
    }

    public static Ledger restore(Long ledgerId, Long walletId, String transactionId,  ReferenceType referenceType, Long referenceId, TransactionType transactionType, BigDecimal amount, BigDecimal balanceSnapshot, String memo, LocalDateTime createdAt) {
        return Ledger.innerBuilder()
                .ledgerId(ledgerId)
                .walletId(walletId)
                .transactionId(transactionId)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .transactionType(transactionType)
                .amount(amount)
                .balanceSnapshot(balanceSnapshot)
                .memo(memo)
                .createdAt(createdAt)
                .build();
    }
}
