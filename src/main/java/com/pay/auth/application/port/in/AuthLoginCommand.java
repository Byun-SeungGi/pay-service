package com.pay.auth.application.port.in;

public record AuthLoginCommand(String email, String password) {
}
