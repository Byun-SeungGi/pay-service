package com.pay.auth.application.service;

import com.pay.auth.application.port.in.AuthLoginCommand;
import com.pay.global.jwt.JwtTokenProvider;
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
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public String login(AuthLoginCommand command) {

        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if(!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if(user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("활동 상태의 계정이 아닙니다.");
        }

        return jwtTokenProvider.createToken(user.getEmail());
    }
}
