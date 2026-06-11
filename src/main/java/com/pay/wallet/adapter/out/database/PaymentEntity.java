package com.pay.wallet.adapter.out.database;

import com.pay.global.entity.BaseTimeEntity;
import com.pay.wallet.domain.Payment;
import com.pay.wallet.domain.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "payment", indexes = {
        @Index(name = "uk_payment_payment_key", columnList = "payment_key", unique = true),
        @Index(name = "uk_payment_order_id", columnList = "order_id", unique = true),
        @Index(name = "idx_payment_user_date", columnList = "user_id, created_at"),
        @Index(name = "idx_payment_status", columnList = "status")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "payment_key", nullable = false)
    private String paymentKey;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "cancel_amount", nullable = false)
    private BigDecimal cancelAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private PaymentStatus status;

    @Column(name = "toss_response", columnDefinition = "TEXT")
    private String tossResponse;

    public static PaymentEntity fromDomain(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.paymentId = payment.getPaymentId();
        entity.paymentKey = payment.getPaymentKey();
        entity.orderId = payment.getOrderId();
        entity.userId = payment.getUserId();
        entity.amount = payment.getAmount();
        entity.cancelAmount = payment.getCancelAmount();
        entity.status = payment.getStatus();
        entity.tossResponse = payment.getTossResponse();
        return entity;
    }

    public Payment toDomain() {
        return Payment.restore(
                this.paymentId,
                this.paymentKey,
                this.orderId,
                this.userId,
                this.amount,
                this.cancelAmount,
                this.status,
                this.tossResponse,
                this.getCreatedAt(),
                this.getUpdatedAt()
        );
    }
}
