package com.pay.wallet.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class Payment {

    private final Long paymentId;
    private final String paymentKey;
    private final String orderId;
    private final Long userId;
    private final BigDecimal amount;
    private final BigDecimal cancelAmount;
    private final PaymentStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder(builderMethodName = "innerBuilder", access = AccessLevel.PRIVATE)
    private Payment(Long paymentId, String paymentKey, String orderId, Long userId, BigDecimal amount, BigDecimal cancelAmount, PaymentStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.paymentId = paymentId;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.cancelAmount = cancelAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Payment create(String paymentKey, String orderId, Long userId, BigDecimal amount) {

        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("결제 금액은 0원보다 커야 합니다.");
        }

        return Payment.innerBuilder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .cancelAmount(BigDecimal.ZERO)
                .status(PaymentStatus.READY)
                .build();
    }

    public static Payment restore(Long paymentId, String paymentKey, String orderId, Long userId, BigDecimal amount, BigDecimal canceledAmount, PaymentStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return Payment.innerBuilder()
                .paymentId(paymentId)
                .paymentKey(paymentKey)
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .cancelAmount(canceledAmount)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public Payment approve() {
        if(this.status != PaymentStatus.READY) {
            throw new IllegalStateException("승인 가능한 상태가 아닙니다.");
        }

        return modifyStatus(PaymentStatus.APPROVED, this.cancelAmount);
    }

    public Payment cancel() {
        if(this.status != PaymentStatus.APPROVED) {
            throw new IllegalStateException("취소 가능한 상태가 아닙니다.");
        }

        return modifyStatus(PaymentStatus.CANCELED, this.amount);
    }

    public Payment cancelPartially(BigDecimal cancelAmount) {
        if(this.status != PaymentStatus.APPROVED && this.status != PaymentStatus.PARTIALLY_CANCELLED) {
            throw new IllegalStateException("취소 가능한 상태가 아닙니다.");
        }

        BigDecimal newCancelAmount = this.cancelAmount.add(cancelAmount);

        PaymentStatus newStatus = newCancelAmount.compareTo(this.amount) == 0
                ? PaymentStatus.CANCELED
                : PaymentStatus.PARTIALLY_CANCELLED;

        return modifyStatus(newStatus, newCancelAmount);
    }

    public Payment fail() {
        if(this.status != PaymentStatus.READY) {
            throw new IllegalStateException("실패 가능한 상태가 아닙니다.");
        }

        return modifyStatus(PaymentStatus.FAILED, this.cancelAmount);
    }

    private Payment modifyStatus(PaymentStatus status, BigDecimal cancelAmount) {
        return Payment.innerBuilder()
                .paymentId(paymentId)
                .paymentKey(paymentKey)
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .cancelAmount(cancelAmount)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
