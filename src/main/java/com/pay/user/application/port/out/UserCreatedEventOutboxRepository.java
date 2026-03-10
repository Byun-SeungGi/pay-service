package com.pay.user.application.port.out;

import com.pay.user.domain.UserCreatedEventOutbox;
import com.pay.user.domain.UserCreatedEventStatus;

import java.util.List;
import java.util.Optional;

public interface UserCreatedEventOutboxRepository {

    UserCreatedEventOutbox save(UserCreatedEventOutbox event);

    List<UserCreatedEventOutbox> findByStatus(UserCreatedEventStatus status);

    Optional<UserCreatedEventOutbox> findByUserId(Long userId);
}
