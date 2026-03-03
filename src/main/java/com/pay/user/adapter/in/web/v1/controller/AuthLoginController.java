package com.pay.user.adapter.in.web.v1.controller;

import com.pay.user.adapter.in.web.v1.dto.AuthLoginRequestDto;
import com.pay.user.adapter.in.web.v1.dto.JwtTokenResponseDto;
import com.pay.user.application.port.in.AuthLoginCommand;
import com.pay.user.application.port.in.AuthLoginUseCase;
import com.pay.global.constant.ApiUrl;
import com.pay.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthLoginController {

    private final AuthLoginUseCase authLoginUseCase;

    /**
     * 로그인 API
     * [POST] /api/users/login
     */
    @Operation(summary = "로그인", description = "이메일, 비밀번호를 이용하여 로그인합니다.")
    @PostMapping(ApiUrl.LOGIN)
    public ApiResponse<JwtTokenResponseDto> login(@RequestBody @Valid AuthLoginRequestDto request) {

        AuthLoginCommand command = new AuthLoginCommand(request.getEmail(), request.getPassword());

        String accessToken = authLoginUseCase.login(command);

        JwtTokenResponseDto response = new JwtTokenResponseDto(accessToken);

        return ApiResponse.success("로그인 성공", response);
    }
}
