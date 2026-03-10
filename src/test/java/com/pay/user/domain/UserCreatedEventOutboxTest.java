package com.pay.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserCreatedEventOutboxTest {

    @Test
    @DisplayName("대기 이벤트 생성 시 상태는 PENDING")
    void pending_creates_event_with_pending_status() {
        UserCreatedEventOutbox event = UserCreatedEventOutbox.pending(1L);

        assertThat(event.getUserId()).isEqualTo(1L);
        assertThat(event.getStatus()).isEqualTo(UserCreatedEventStatus.PENDING);
    }

    @Test
    @DisplayName("complete 호출 시 상태가 COMPLETED로 변경")
    void complete_changes_status_to_completed() {
        UserCreatedEventOutbox event = UserCreatedEventOutbox.pending(1L);

        event.complete();

        assertThat(event.getStatus()).isEqualTo(UserCreatedEventStatus.COMPLETED);
    }
}
