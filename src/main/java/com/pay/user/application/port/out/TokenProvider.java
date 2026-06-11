package com.pay.user.application.port.out;

public interface TokenProvider {

    String createToken(String email, Long userId);
}