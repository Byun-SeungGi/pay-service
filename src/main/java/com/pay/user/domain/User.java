package com.pay.user.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
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

    @Builder(builderMethodName = "innerBuilder", access = AccessLevel.PRIVATE)
    private User(Long userId, UUID externalId, String email, String password, String phoneNumber, UserStatus status, LocalDateTime withdrawnAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.externalId = externalId;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.withdrawnAt = withdrawnAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User create(String email, String password, String phoneNumber) {
        return User.innerBuilder()
                .email(email)
                .password(password)
                .phoneNumber(phoneNumber)
                .externalId(UUID.randomUUID())
                .status(UserStatus.ACTIVE)
                .build();
    }

    public static User restore(Long userId, UUID externalId, String email, String password, String phoneNumber, UserStatus status, LocalDateTime withdrawnAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return User.innerBuilder()
                .userId(userId)
                .externalId(externalId)
                .email(email)
                .password(password)
                .phoneNumber(phoneNumber)
                .status(status)
                .withdrawnAt(withdrawnAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public void withdraw() {
        if (this.status == UserStatus.WITHDRAWN) {
            throw new IllegalStateException("이미 탈퇴한 회원입니다.");
        }
        this.status = UserStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }

    public static User restoreForAuth(Long userId, String email) {
        return User.innerBuilder()
                .userId(userId)
                .email(email)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
