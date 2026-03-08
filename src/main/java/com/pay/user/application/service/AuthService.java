package com.pay.user.application.service;

import com.pay.user.application.port.in.AuthLoginCommand;
import com.pay.user.application.port.in.AuthLoginUseCase;
import com.pay.user.application.port.out.TokenProvider;
import com.pay.user.application.port.out.UserRepository;
import com.pay.user.domain.User;
import com.pay.user.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService implements AuthLoginUseCase {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public String login(AuthLoginCommand command) {

        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if(!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if(user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("활동 상태의 계정이 아닙니다.");
        }

        return tokenProvider.createToken(user.getEmail());
    }
}
