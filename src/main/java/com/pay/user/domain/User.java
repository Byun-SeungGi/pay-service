package com.pay.user.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class User {

    private Long userId;
    private UUID externalId;
    private String email;
    private String password;
    private String phoneNumber;
    private UserStatus status;
    private LocalDateTime withdrawnAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static User create(String email, String password, String phoneNumber) {
        return User.builder()
                .email(email)
                .password(password)
                .phoneNumber(phoneNumber)
                .externalId(UUID.randomUUID())
                .status(UserStatus.ACTIVE)
                .build();
    }

    public void withdraw() {
        if (this.status == UserStatus.WITHDRAWN) {
            throw new IllegalStateException("이미 탈퇴한 회원입니다.");
        }
        this.status = UserStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }

}
