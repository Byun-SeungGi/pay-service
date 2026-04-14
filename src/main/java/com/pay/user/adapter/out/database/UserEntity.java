package com.pay.user.adapter.out.database;

import com.pay.global.entity.BaseTimeEntity;
import com.pay.user.domain.User;
import com.pay.user.domain.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "external_id", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    private UUID externalId;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private UserStatus status;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    // 도메인 → 엔티티
    public static UserEntity fromDomain(User user) {
        UserEntity entity = new UserEntity();
        entity.userId = user.getUserId();
        entity.externalId = user.getExternalId();
        entity.email = user.getEmail();
        entity.password = user.getPassword();
        entity.phoneNumber = user.getPhoneNumber();
        entity.status = user.getStatus();
        entity.withdrawnAt = user.getWithdrawnAt();
        return entity;
    }

    // 엔티티 → 도메인
    public User toDomain() {
        return User.restore(
                this.userId,
                this.externalId,
                this.email,
                this.password,
                this.phoneNumber,
                this.status,
                this.withdrawnAt,
                this.getCreatedAt(),
                this.getUpdatedAt()
        );
    }
}
