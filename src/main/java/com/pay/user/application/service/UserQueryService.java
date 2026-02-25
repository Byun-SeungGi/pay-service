package com.pay.user.application.service;

import com.pay.user.application.port.in.UserProfileResult;
import com.pay.user.application.port.in.UserQueryUseCase;
import com.pay.user.application.port.out.UserRepository;
import com.pay.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService implements UserQueryUseCase {

    private final UserRepository userRepository;

    @Override
    public UserProfileResult getMyProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일 입니다."));

        return new UserProfileResult(user.getEmail(), user.getPhoneNumber());
    }
}
