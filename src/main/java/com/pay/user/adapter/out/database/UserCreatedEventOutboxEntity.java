package com.pay.user.adapter.out.database;

import com.pay.global.entity.BaseTimeEntity;
import com.pay.user.domain.UserCreatedEventOutbox;
import com.pay.user.domain.UserCreatedEventStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name= "user_created_event_outbox")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCreatedEventOutboxEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserCreatedEventStatus status;

    public static UserCreatedEventOutboxEntity fromDomain(UserCreatedEventOutbox event) {
        UserCreatedEventOutboxEntity entity = new UserCreatedEventOutboxEntity();
        entity.eventId = event.getEventId();
        entity.userId = event.getUserId();
        entity.status = event.getStatus();
        return entity;
    }

    public UserCreatedEventOutbox toDomain() {
        return UserCreatedEventOutbox.restore(
                this.eventId,
                this.userId,
                this.status,
                this.getCreatedAt(),
                this.getUpdatedAt()
        );
    }
}
