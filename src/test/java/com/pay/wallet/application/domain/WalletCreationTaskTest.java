package com.pay.wallet.application.domain;

import com.pay.wallet.domain.WalletCreationTask;
import com.pay.wallet.domain.WalletCreationTaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WalletCreationTaskTest {

    @Test
    @DisplayName("실패 작업 생성 시 상태는 FAILED")
    void fail_creates_task_with_failed_status() {
        WalletCreationTask task = WalletCreationTask.fail(1L);

        assertThat(task.getUserId()).isEqualTo(1L);
        assertThat(task.getStatus()).isEqualTo(WalletCreationTaskStatus.FAILED);
    }

    @Test
    @DisplayName("complete 호출 시 상태가 COMPLETED로 변경")
    void complete_changes_status_to_completed() {
        WalletCreationTask task = WalletCreationTask.fail(1L);

        task.complete();

        assertThat(task.getStatus()).isEqualTo(WalletCreationTaskStatus.COMPLETED);
    }
}