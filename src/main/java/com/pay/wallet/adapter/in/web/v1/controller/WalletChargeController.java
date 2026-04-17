package com.pay.wallet.adapter.in.web.v1.controller;

import com.pay.global.constant.ApiUrl;
import com.pay.global.response.ApiResponse;

import com.pay.user.adapter.in.security.CustomUserDetails;
import com.pay.wallet.adapter.in.web.v1.dto.WalletChargeRequestDto;
import com.pay.wallet.application.port.in.WalletChargeUseCase;
import com.pay.wallet.domain.Wallet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

@Tag(name = "3. 지갑관리", description = "지갑충전")
@RestController
@RequiredArgsConstructor
public class WalletChargeController {

    private final WalletChargeUseCase walletChargeUseCase;
    
    @Operation(summary = "금액 충전", description = "userId에 해당하는 지갑에 amount를 이용하여 충전합니다.")
    @PostMapping(ApiUrl.CHARGE)
    public ApiResponse<BigDecimal> charge(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid WalletChargeRequestDto request, @RequestHeader("Idempotency-Key") String transactionId) {

        Instant keyCreatedAt = extractTimestamp(transactionId);
        if (keyCreatedAt.isBefore(Instant.now().minus(Duration.ofDays(4)))) {
            throw new IllegalArgumentException("만료된 멱등성 키입니다.");
        }

        Long userId = userDetails.getUser().getUserId();
        Wallet wallet = walletChargeUseCase.charge(userId, request.amount(), transactionId);

        return ApiResponse.success("충전이 완료되었습니다.", wallet.getBalance());
    }

    private Instant extractTimestamp(String uuid) {
        long timestamp = Long.parseLong(uuid.replace("-", "").substring(0, 12), 16);
        return Instant.ofEpochMilli(timestamp);
    }
}
