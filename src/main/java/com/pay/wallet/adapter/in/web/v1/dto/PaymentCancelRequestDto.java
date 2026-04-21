package com.pay.wallet.adapter.in.web.v1.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentCancelRequestDto(

        @NotNull(message = "결제 ID는 필수입니다.")
        Long paymentId,

        String cancelAmount,

        @NotNull(message = "취소사유는 필수입니다.")
        String cancelReason,

        @NotNull(message = "멱등성키는 필수입니다.")
        String idempotencyKey
) {
        public BigDecimal parseCancelAmount() {
                if(cancelAmount == null || cancelAmount.isBlank()) {
                        return null;
                }

                BigDecimal amount = new BigDecimal(cancelAmount);
                if(amount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("취소금액은 0보다 커야 합니다.");
                }

                return amount;
         }


}
