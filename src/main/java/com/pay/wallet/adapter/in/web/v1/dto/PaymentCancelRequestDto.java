package com.pay.wallet.adapter.in.web.v1.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentCancelRequestDto(

        @NotNull(message = "결제 ID는 필수입니다.")
        Long paymentId,

        @Positive(message = "취소금액은 0보다 커야 합니다.")
        BigDecimal cancelAmount,

        @NotNull(message = "취소사유는 필수입니다.")
        String cancelReason,

        @NotNull(message = "멱등성키는 필수입니다.")
        String idempotencyKey
) {}
