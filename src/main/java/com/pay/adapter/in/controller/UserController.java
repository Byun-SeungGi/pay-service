package com.pay.adapter.in.controller;

import com.pay.adapter.in.controller.request.RegisterUserRequest;
import com.pay.adapter.in.controller.response.RegisterUserResponse;
import com.pay.application.in.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/v1/users")
    public ApiResponse<RegisterUserResponse> register(RegisterUserRequest user) {
        userService.register();
    }
}
