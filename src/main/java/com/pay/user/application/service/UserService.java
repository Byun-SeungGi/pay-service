package com.pay.user.application.service;

import com.pay.user.application.port.in.SignUpCommand;
import com.pay.user.application.port.in.SignUpUseCase;
import com.pay.user.domain.User;
import com.pay.user.application.port.out.UserRepository;
import com.pay.user.domain.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements SignUpUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 회원가입
     * @param command 회원가입 요청 정보
     * @return 생성된 회원의 PK (userId)
     */
    @Transactional
    public Long signUp(SignUpCommand command) {

        // 중복체크
        validateDuplicateUser(command);
        
        // 비밀번호 단방향 해싱
        String encPassword = passwordEncoder.encode(command.password());
        
        // 도메인 객체 생성
        User newUser = User.create(
                command.email(),
                encPassword,
                command.phoneNumber()
        );

        // 저장
        User saveUser = userRepository.save(newUser);

        eventPublisher.publishEvent(new UserCreatedEvent(saveUser.getUserId()));

        return saveUser.getUserId();
    }

    private void validateDuplicateUser(SignUpCommand command) {
        if(userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        if(userRepository.existsByPhoneNumber(command.phoneNumber())) {
            throw new IllegalArgumentException("이미 사용중인 전화번호입니다.");
        }
    }
}
