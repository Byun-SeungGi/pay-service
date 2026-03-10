package com.pay.user.adapter.out.database;

import com.pay.user.application.port.out.UserCreatedEventOutboxRepository;
import com.pay.user.domain.UserCreatedEventOutbox;
import com.pay.user.domain.UserCreatedEventStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserCreatedEventOutboxRepositoryAdapter implements UserCreatedEventOutboxRepository {

    private final UserCreatedEventOutboxJpaRepository userCreatedEventOutboxJpaRepository;

    @Override
    public UserCreatedEventOutbox save(UserCreatedEventOutbox event) {
        UserCreatedEventOutboxEntity entity = UserCreatedEventOutboxEntity.fromDomain(event);
        UserCreatedEventOutboxEntity saved = userCreatedEventOutboxJpaRepository.save(entity);

        return saved.toDomain();
    }

    @Override
    public List<UserCreatedEventOutbox> findByStatus(UserCreatedEventStatus status) {
        return userCreatedEventOutboxJpaRepository.findByStatus(status)
                .stream()
                .map(UserCreatedEventOutboxEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<UserCreatedEventOutbox> findByUserId(Long userId) {
        return userCreatedEventOutboxJpaRepository.findByUserId(userId)
                .map(UserCreatedEventOutboxEntity::toDomain);
    }
}
