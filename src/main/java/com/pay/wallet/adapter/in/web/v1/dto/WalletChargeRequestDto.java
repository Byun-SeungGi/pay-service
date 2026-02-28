package com.pay.wallet.adapter.in.web.v1.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WalletChargeRequestDto(
        @NotNull(message = "충전 금액은 필수입니다.")
        BigDecimal amount
) { }
