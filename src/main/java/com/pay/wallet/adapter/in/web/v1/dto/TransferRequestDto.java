package com.pay.wallet.adapter.in.web.v1.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequestDto(
        @NotNull(message = "받는 사람은 필수입니다.")
        Long receiverUserId,

        @NotNull(message = "송금 금액은 필수입니다.")
        @Positive(message = "송금 금액은 0원보다 커야 합니다.")
        BigDecimal amount,

        String memo
) {}
