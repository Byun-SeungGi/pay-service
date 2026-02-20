package com.pay.user.application.service;

import com.pay.user.adapter.in.web.v1.dto.UserProfileResponseDto;
import com.pay.user.application.port.in.UserQueryUseCase;
import com.pay.user.application.port.out.UserRepository;
import com.pay.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService implements UserQueryUseCase {

    private final UserRepository userRepository;

    @Override
    public UserProfileResponseDto getMyProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일 입니다."));

        return new UserProfileResponseDto(user.getEmail(), user.getPhoneNumber());
    }
}
