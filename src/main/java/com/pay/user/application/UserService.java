package com.pay.user.application;

import com.pay.user.api.v1.dto.UserSignUpRequestDto;
import com.pay.user.domain.User;
import com.pay.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     * @param request 회원가입 요청 정보
     * @return 생성된 회원의 PK (userId)
     */
    @Transactional
    public Long signUp(UserSignUpRequestDto request) {

        // 중복체크
        validateDuplicateUser(request);
        
        // 비밀번호 단방향 해싱
        String encPassword = passwordEncoder.encode(request.getPassword());

        // Entity 변환
        User newUser = request.toEntity(encPassword);

        // 저장
        User saveUser = userRepository.save(newUser);

        return saveUser.getUserId();
    }

    private void validateDuplicateUser(UserSignUpRequestDto request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        if(userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("이미 사용중인 전화번호입니다.");
        }
    }
}
