package com.pay.user.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserCreatedEventOutbox {

    private final Long eventId;
    private final Long userId;
    private UserCreatedEventStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder(builderMethodName = "innerBuilder", access = AccessLevel.PRIVATE)
    private UserCreatedEventOutbox(Long eventId, Long userId, UserCreatedEventStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.eventId = eventId;
        this.userId = userId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserCreatedEventOutbox pending(Long userId) {
        return UserCreatedEventOutbox.innerBuilder()
                .userId(userId)
                .status(UserCreatedEventStatus.PENDING)
                .build();
    }

    public static UserCreatedEventOutbox restore(Long eventId, Long userId, UserCreatedEventStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return UserCreatedEventOutbox.innerBuilder()
                .eventId(eventId)
                .userId(userId)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public void complete() {
        this.status = UserCreatedEventStatus.COMPLETED;
    }
}
