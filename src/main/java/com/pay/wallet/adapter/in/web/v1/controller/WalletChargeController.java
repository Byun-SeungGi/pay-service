package com.pay.wallet.adapter.in.web.v1.controller;

import com.pay.global.constant.ApiUrl;
import com.pay.global.response.ApiResponse;
import com.pay.global.security.CustomUserDetails;
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
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Tag(name = "2. 지갑관리", description = "지갑충전")
@RestController
@RequiredArgsConstructor
public class WalletChargeController {

    private final WalletChargeUseCase walletChargeUseCase;
    
    @Operation(summary = "금액 충전", description = "userId에 해당하는 지갑에 amount를 이용하여 충전합니다.")
    @PostMapping(ApiUrl.CHARGE)
    public ApiResponse<BigDecimal> charge(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid WalletChargeRequestDto request) {

        Long userId = userDetails.getUser().getUserId();
        Wallet wallet = walletChargeUseCase.charge(userId, request.amount());

        return ApiResponse.success("충전이 완료되었습니다.", wallet.getBalance());
    }
}
