package com.pay.wallet.adapter.in.web.v1.controller;

import com.pay.global.constant.ApiUrl;
import com.pay.global.response.ApiResponse;
import com.pay.wallet.adapter.in.web.v1.dto.TransferRequestDto;
import com.pay.wallet.application.port.in.TransferUseCase;
import com.pay.wallet.domain.Transfer;
import com.pay.user.adapter.in.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "4. 송금", description = "송금 기능")
@RestController
@RequiredArgsConstructor
public class TransferController {

    private final TransferUseCase transferUseCase;

    @Operation(summary = "송금", description = "다른 사용자에게 송금합니다.")
    @PostMapping(ApiUrl.TRANSFER_URL)
    public ApiResponse<Long> transfer(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid TransferRequestDto request, @RequestHeader("Idempotency-Key") String transactionId) {

        Long senderUserId = userDetails.getUser().getUserId();

        Transfer transfer = transferUseCase.transfer(
                senderUserId,
                request.receiverUserId(),
                request.amount(),
                transactionId,
                request.memo()
        );

        if(!transfer.isCompleted()) {
            return ApiResponse.fail("송금에 실패했습니다.");
        }

        return ApiResponse.success("송금이 완료되었습니다.", transfer.getTransferId());
    }
}
