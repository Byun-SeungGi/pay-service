package com.pay.transfer.adapter.out.database;

import com.pay.global.entity.BaseTimeEntity;
import com.pay.transfer.domain.Transfer;
import com.pay.transfer.domain.TransferStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "transfer")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransferEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id")
    private Long transferId;

    @Column(name = "sender_user_id", nullable = false)
    private Long senderUserId;

    @Column(name = "receiver_user_id", nullable = false)
    private Long receiverUserId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferStatus status;

    @Column(name = "memo")
    private String memo;

    public static TransferEntity fromDomain(Transfer transfer) {
        TransferEntity entity = new TransferEntity();
        entity.transferId = transfer.getTransferId();
        entity.senderUserId = transfer.getSenderUserId();
        entity.receiverUserId = transfer.getReceiverUserId();
        entity.amount = transfer.getAmount();
        entity.transactionId = transfer.getTransactionId();
        entity.status = transfer.getStatus();
        entity.memo = transfer.getMemo();
        return entity;
    }

    public Transfer toDomain() {
        return Transfer.restore(
                this.transferId,
                this.senderUserId,
                this.receiverUserId,
                this.amount,
                this.transactionId,
                this.status,
                this.memo,
                this.getCreatedAt(),
                this.getUpdatedAt()
        );
    }
}
