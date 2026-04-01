package com.pay.transfer.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class Transfer {

    private final Long transferId;
    private final Long senderUserId;
    private final Long receiverUserId;
    private final BigDecimal amount;
    private final String transactionId;
    private final TransferStatus status;
    private final String memo;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder(builderMethodName = "innerBuilder", access = AccessLevel.PRIVATE)
    private Transfer(Long transferId, Long senderUserId, Long receiverUserId, BigDecimal amount, String transactionId, TransferStatus status, String memo, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.transferId = transferId;
        this.senderUserId = senderUserId;
        this.receiverUserId = receiverUserId;
        this.amount = amount;
        this.transactionId = transactionId;
        this.status = status;
        this.memo = memo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Transfer create(Long senderUserId, Long receiverUserId, BigDecimal amount, String transactionId, String memo) {

        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("송금 금액은 0보다 커야 합니다.");
        }

        if(senderUserId.equals(receiverUserId)) {
            throw new IllegalArgumentException("자기 자신에게 송금할 수 없습니다.");
        }

        return Transfer.innerBuilder()
                .senderUserId(senderUserId)
                .receiverUserId(receiverUserId)
                .amount(amount)
                .transactionId(transactionId)
                .status(TransferStatus.REQUESTED)
                .memo(memo)
                .build();
    }

    public static Transfer restore(Long transferId, Long senderUserId, Long receiverUserId, BigDecimal amount, String transactionId, TransferStatus status, String memo, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return Transfer.innerBuilder()
                .transferId(transferId)
                .senderUserId(senderUserId)
                .receiverUserId(receiverUserId)
                .amount(amount)
                .transactionId(transactionId)
                .status(status)
                .memo(memo)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public Transfer withDraw() {
        if(this.status != TransferStatus.REQUESTED) {
            throw new IllegalStateException("출금 가능한 상태가 아닙니다.");
        }
        return modifyStatus(TransferStatus.WITHDRAWN);
    }

    public Transfer complete() {
        if(this.status != TransferStatus.WITHDRAWN) {
            throw new IllegalStateException("완료 가능한 상태가 아닙니다.");
        }
        return modifyStatus(TransferStatus.COMPLETED);
    }

    public Transfer fail() {
        if(this.status != TransferStatus.REQUESTED) {
            throw new IllegalStateException("실패 처리가 가능한 상태가 아닙니다.");
        }
        return modifyStatus(TransferStatus.FAILED);
    }

    public Transfer refund() {
        if(this.status != TransferStatus.WITHDRAWN) {
            throw new IllegalStateException("환불 가능한 상태가 아닙니다.");
        }
        return modifyStatus(TransferStatus.REFUNDED);
    }

    public Transfer refundFail() {
        if(this.status != TransferStatus.WITHDRAWN) {
            throw new IllegalStateException("환불 처리가 가능한 상태가 아닙니다.");
        }
        return modifyStatus(TransferStatus.REFUND_FAILED);
    }

    public boolean isCompleted() {
        return this.status == TransferStatus.COMPLETED;
    }


    private Transfer modifyStatus(TransferStatus status) {
        return Transfer.innerBuilder()
                .transferId(this.transferId)
                .senderUserId(this.senderUserId)
                .receiverUserId(this.receiverUserId)
                .amount(this.amount)
                .transactionId(this.transactionId)
                .status(status)
                .memo(this.memo)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}