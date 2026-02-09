package com.pay.user.api;

import com.pay.global.response.ApiResponse;
import com.pay.user.api.dto.UserSignUpRequestDto;
import com.pay.user.application.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * 회원가입 API
     * [POST] /api/users/signUp
     */
    @PostMapping("/signUp")
    public ApiResponse<Long> signUp(@RequestBody @Valid UserSignUpRequestDto request) {

        Long userId = userService.signUp(request);

        return ApiResponse.success("회원가입에 성공했습니다.", userId);
    }
}
