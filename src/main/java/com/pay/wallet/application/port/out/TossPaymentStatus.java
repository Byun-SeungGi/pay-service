package com.pay.wallet.application.port.out;

public record TossPaymentStatus(Action action, String rawResponse) {

    public enum Action {
        APPROVE,        // 토스 DONE -> 이 프로젝트 DB APPROVED와 매핑
        FAIL,           // 토스 ABORTED/EXPIRED/NOT_FOUND 등 -> 이 프로젝트 DB FAILED와 매핑
        SKIP            // 토스 READY/IN_PROGRESS -> 다음 배치에서 재시도
    }
}
