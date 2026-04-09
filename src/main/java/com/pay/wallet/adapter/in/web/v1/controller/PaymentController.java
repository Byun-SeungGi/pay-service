package com.pay.wallet.adapter.in.web.v1.controller;

import com.pay.global.constant.ApiUrl;
import com.pay.global.response.ApiResponse;
import com.pay.user.adapter.in.security.CustomUserDetails;
import com.pay.wallet.application.port.in.PaymentConfirmUseCase;
import com.pay.wallet.domain.Payment;
import com.pay.wallet.domain.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentConfirmUseCase paymentConfirmUseCase;

    @PostMapping(ApiUrl.PAYMENT_URL)
    public ApiResponse<Long> confirmPayment(@AuthenticationPrincipal CustomUserDetails userDetails
            , @RequestParam String paymentKey, @RequestParam String orderId, @RequestParam BigDecimal amount) {

        Payment payment = paymentConfirmUseCase.confirm(
                paymentKey,
                orderId,
                userDetails.getUser().getUserId(),
                amount);

        return ApiResponse.success("결제 승인이 완료되었습니다.", payment.getPaymentId());
    }

}
