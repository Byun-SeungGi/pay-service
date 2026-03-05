package com.pay.user.application.service;

import com.pay.user.application.port.in.AuthLoginCommand;
import com.pay.user.application.port.out.TokenProvider;
import com.pay.user.application.port.out.UserRepository;
import com.pay.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("로그인 성공 시 JWT 토큰 반환")
    void login_success() throws Exception {
        AuthLoginCommand command = new AuthLoginCommand("gowawa@naver.com", "q!w2e3r4t5");
        User fakeUser = User.create("gowawa@naver.com", "q!w2e3r4t5", "010-1234-5678");

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(fakeUser));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(tokenProvider.createToken("gowawa@naver.com")).willReturn("mock.jwt.token");

        String token = authService.login(command);

        assertThat(token).isEqualTo("mock.jwt.token");
    }

    @Test
    @DisplayName("가입되지 않은 이메일 예외 발생")
    void login_fail() throws Exception {
        AuthLoginCommand command = new AuthLoginCommand("asdfadfasdf@naver.com", "password123!");

        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}