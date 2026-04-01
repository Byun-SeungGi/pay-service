package com.pay.transfer.domain;

public enum TransferStatus {
    REQUESTED,     // 송금요청
    WITHDRAWN,     // 출금완료
    COMPLETED,     // 송금완료
    FAILED,        // 실패
    REFUNDED,       // 환불완료
    REFUND_FAILED  //환불실패
}
