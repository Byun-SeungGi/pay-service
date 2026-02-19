package com.pay.user.application.port.in;

public record SignUpCommand(
        String email,
        String password,
        String phoneNumber
) {}
