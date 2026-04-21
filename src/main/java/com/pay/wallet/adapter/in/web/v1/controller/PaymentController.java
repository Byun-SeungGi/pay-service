package com.pay.wallet.adapter.in.web.v1.controller;

import com.pay.global.constant.ApiUrl;
import com.pay.global.response.ApiResponse;
import com.pay.user.adapter.in.security.CustomUserDetails;
import com.pay.wallet.adapter.in.web.v1.dto.PaymentCancelRequestDto;
import com.pay.wallet.application.port.in.PaymentCancelUseCase;
import com.pay.wallet.application.port.in.PaymentConfirmUseCase;
import com.pay.wallet.application.port.in.PaymentFindUseCase;
import com.pay.wallet.domain.Payment;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "5. 결제관리", description = "토스페이먼츠 결제 API")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentConfirmUseCase paymentConfirmUseCase;
    private final PaymentCancelUseCase paymentCancelUseCase;
    private final PaymentFindUseCase paymentFindUseCase;

    @Hidden
    @PostMapping(ApiUrl.PAYMENT_CONFIRM_URL)
    public ApiResponse<Long> confirmPayment(@AuthenticationPrincipal CustomUserDetails userDetails
            , @RequestParam String paymentKey, @RequestParam String orderId, @RequestParam BigDecimal amount) {

        Payment payment = paymentConfirmUseCase.confirm(
                paymentKey,
                orderId,
                userDetails.getUser().getUserId(),
                amount);

        return ApiResponse.success("결제 승인이 완료되었습니다.", payment.getPaymentId());
    }

    @Operation(summary = "내 결제 내역 조회", description = "유저ID를 기준으로 내 결제 내역을 조회합니다.")
    @GetMapping(ApiUrl.PAYMENT_FIND_URL)
    public ApiResponse<List<Payment>> findPayments(@AuthenticationPrincipal CustomUserDetails userDetails) {

        List<Payment> findPayments = paymentFindUseCase.findByUserId(userDetails.getUser().getUserId());

        return ApiResponse.success("결제 내역 조회에 성공했습니다.", findPayments);
    }

    @Operation(summary = "결제취소", description = "결제 ID를 기준으로 결제를 취소합니다.")
    @PostMapping(ApiUrl.PAYMENT_CANCEL_URL)
    public ApiResponse<Long> cancelPayment(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid PaymentCancelRequestDto request) {

        Payment payment = paymentCancelUseCase.cancel(
                request.paymentId(),
                request.parseCancelAmount(),
                userDetails.getUser().getUserId(),
                request.idempotencyKey(),
                request.cancelReason()
        );

        return ApiResponse.success("결제 취소가 완료되었습니다.", payment.getPaymentId());
    }
}
