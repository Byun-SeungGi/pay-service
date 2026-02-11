package com.pay.user.api.v1.controller;

import com.pay.global.response.ApiResponse;
import com.pay.user.api.v1.dto.UserSignUpRequestDto;
import com.pay.user.application.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "1. 회원관리", description = "회원가입")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * 회원가입 API
     * [POST] /api/users/signUp
     */
    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 전화번호를 기준으로 신규유저를 등록합니다.")
    @PostMapping("/signUp")
    public ApiResponse<Long> signUp(@RequestBody @Valid UserSignUpRequestDto request) {

        Long userId = userService.signUp(request);

        return ApiResponse.success("회원가입에 성공했습니다.", userId);
    }
}
