package com.pay.user.adapter.out.database;

import com.pay.user.domain.UserCreatedEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCreatedEventOutboxJpaRepository extends JpaRepository<UserCreatedEventOutboxEntity, Long> {

    List<UserCreatedEventOutboxEntity> findByStatus(UserCreatedEventStatus status);

    Optional<UserCreatedEventOutboxEntity> findByUserId(Long userId);
}
