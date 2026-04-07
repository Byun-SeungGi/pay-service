package com.pay.wallet.domain;

public enum PaymentStatus {
    READY,                  // 결제 생성
    APPROVED,               // 토스 승인 완료
    PARTIALLY_CANCELLED,    // 부분 취소
    CANCELED,               // 결제 취소
    FAILED                  // 토스 승인 실패
}
