package com.pay.user.application;

import com.pay.user.application.port.in.SignUpCommand;
import com.pay.user.application.service.UserService;
import com.pay.user.domain.User;
import com.pay.user.application.port.out.UserRepository;
import com.pay.user.domain.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 성공")
    void signUp_success() {

        SignUpCommand command = new SignUpCommand("test@email.com", "password123@asd", "010-1234-5678");

        given(userRepository.existsByEmail(any())).willReturn(false);
        given(userRepository.existsByPhoneNumber(any())).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encodedPassword");

        User fakeUser = User.builder()
                .userId(1L)
                .externalId(UUID.randomUUID())
                .email("test@email.com")
                .password("encodedPassword")
                .phoneNumber("010-1234-5678")
                .status(UserStatus.ACTIVE)
                .build();

        given(userRepository.save(any())).willReturn(fakeUser);

        Long savedId = userService.signUp(command);

        assertThat(savedId).isEqualTo(1L);
        verify(userRepository, times(1)).save(any());
        verify(passwordEncoder, times(1)).encode(any());

    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signUp_fail_duplicate_email() {

        SignUpCommand command = new SignUpCommand("duplicate@email.com", "1234", "010-1111-2222");

        given(userRepository.existsByEmail(command.email())).willReturn(true);

        assertThatThrownBy(() -> userService.signUp(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용중인 이메일입니다.");
    }

    @Test
    @DisplayName("회원가입 실패 - 전화번호 중복")
    void signUp_fail_duplicate_phone() {
        // given
        SignUpCommand command = new SignUpCommand("new@email.com", "1234", "010-9999-9999");

        given(userRepository.existsByEmail(command.email())).willReturn(false);
        given(userRepository.existsByPhoneNumber(command.phoneNumber())).willReturn(true);

        assertThatThrownBy(() -> userService.signUp(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용중인 전화번호입니다.");
    }
}