package com.pay.user.adapter.in.web.v1.controller;

import com.pay.global.constant.ApiUrl;
import com.pay.global.response.ApiResponse;
import com.pay.user.adapter.in.web.v1.dto.UserSignUpRequestDto;
import com.pay.user.application.port.in.SignUpCommand;
import com.pay.user.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "1. 회원관리", description = "회원가입")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입 API
     * [POST] /api/users/signUp
     */
    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 전화번호를 기준으로 신규유저를 등록합니다.")
    @PostMapping(ApiUrl.SIGN_UP)
    public ApiResponse<Long> signUp(@RequestBody @Valid UserSignUpRequestDto request) {
        SignUpCommand command = new SignUpCommand(request.getEmail(), request.getPassword(), request.getPhoneNumber());

        Long userId = userService.signUp(command);

        return ApiResponse.success("회원가입에 성공했습니다.", userId);
    }
}
