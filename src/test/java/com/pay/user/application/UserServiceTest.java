package com.pay.user.application;

import com.pay.user.api.v1.dto.UserSignUpRequestDto;
import com.pay.user.domain.User;
import com.pay.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
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

        UserSignUpRequestDto request = new UserSignUpRequestDto("test@email.com", "password123@asd", "010-1234-5678");

        given(userRepository.existsByEmail(any())).willReturn(false);
        given(userRepository.existsByPhoneNumber(any())).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encodedPassword");

        User fakeUser = request.toEntity("encodedPassword");
        ReflectionTestUtils.setField(fakeUser, "userId", 1L);
        given(userRepository.save(any())).willReturn(fakeUser);

        Long savedId = userService.signUp(request);

        assertThat(savedId).isEqualTo(1L);
        verify(userRepository, times(1)).save(any());
        verify(passwordEncoder, times(1)).encode(any());

    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signUp_fail_duplicate_email() {

        UserSignUpRequestDto request = new UserSignUpRequestDto("duplicate@email.com", "1234", "010-1111-2222");

        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        assertThatThrownBy(() -> userService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용중인 이메일입니다.");
    }

    @Test
    @DisplayName("회원가입 실패 - 전화번호 중복")
    void signUp_fail_duplicate_phone() {
        // given
        UserSignUpRequestDto request = new UserSignUpRequestDto("new@email.com", "1234", "010-9999-9999");

        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(userRepository.existsByPhoneNumber(request.getPhoneNumber())).willReturn(true);

        assertThatThrownBy(() -> userService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용중인 전화번호입니다.");
    }
}