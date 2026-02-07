package com.pay.user.domain;

import com.pay.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    // 유저ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    // 외부 식별 ID
    @Column(name = "external_id", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    private UUID externalId;

    // 이메일 겸 로그인 ID
    @Column(unique = true, length = 100)
    private String email;

    // 단방향 해시된 비밀번호
    @Column(nullable = false, length = 255)
    private String password;

    // 핸드폰번호
    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    // 상태관리
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    // 탈퇴일시
    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Builder
    public User(String email, String password, String phoneNumber) {
        this.externalId = UUID.randomUUID();
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.status = UserStatus.ACTIVE;
    }

    public void withdraw() {
        if(this.status == UserStatus.WITHDRAWN) {
            throw new IllegalStateException("이미 탈퇴한 회원입니다.");
        }
        this.status = UserStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }
}
