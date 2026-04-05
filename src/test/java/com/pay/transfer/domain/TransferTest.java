package com.pay.transfer.domain;

import com.pay.wallet.domain.Transfer;
import com.pay.wallet.domain.TransferStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransferTest {

    @Test
    @DisplayName("송금 생성 성공")
    void create_success() {
        Transfer transfer = Transfer.create(1L, 2L, BigDecimal.valueOf(10000), "tx-001", "테스트 송금");

        assertThat(transfer.getSenderUserId()).isEqualTo(1L);
        assertThat(transfer.getReceiverUserId()).isEqualTo(2L);
        assertThat(transfer.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(transfer.getTransactionId()).isEqualTo("tx-001");
        assertThat(transfer.getStatus()).isEqualTo(TransferStatus.REQUESTED);
        assertThat(transfer.getMemo()).isEqualTo("테스트 송금");
    }

    @Test
    @DisplayName("송금 생성 실패 - 금액 0원")
    void create_fail_zero_amount() {
        assertThatThrownBy(() -> Transfer.create(1L, 2L, BigDecimal.ZERO, "tx-001", "메모"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("송금 금액은 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("송금 생성 실패 - 금액 null")
    void create_fail_null_amount() {
        assertThatThrownBy(() -> Transfer.create(1L, 2L, null, "tx-001", "메모"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("송금 금액은 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("송금 생성 실패 - 음수 금액")
    void create_fail_negative_amount() {
        assertThatThrownBy(() -> Transfer.create(1L, 2L, BigDecimal.valueOf(-1000), "tx-001", "메모"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("송금 금액은 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("송금 생성 실패 - 자기 자신에게 송금")
    void create_fail_self_transfer() {
        assertThatThrownBy(() -> Transfer.create(1L, 1L, BigDecimal.valueOf(10000), "tx-001", "메모"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("자기 자신에게 송금할 수 없습니다.");
    }

    @Test
    @DisplayName("완료 상태 전이 성공 - REQUESTED → COMPLETED")
    void complete_success() {
        Transfer transfer = Transfer.create(1L, 2L, BigDecimal.valueOf(10000), "tx-001", "메모");

        Transfer completed = transfer.complete();

        assertThat(completed.getStatus()).isEqualTo(TransferStatus.COMPLETED);
    }

    @Test
    @DisplayName("완료 상태 전이 실패 - REQUESTED가 아닌 상태")
    void complete_fail_invalid_status() {
        Transfer transfer = createTransferWithStatus(TransferStatus.COMPLETED);

        assertThatThrownBy(transfer::complete)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("완료 가능한 상태가 아닙니다.");
    }

    @Test
    @DisplayName("실패 상태 전이 성공")
    void fail_success() {
        Transfer transfer = Transfer.create(1L, 2L, BigDecimal.valueOf(10000), "tx-001", "메모");

        Transfer failed = transfer.fail();

        assertThat(failed.getStatus()).isEqualTo(TransferStatus.FAILED);
    }

    private Transfer createTransferWithStatus(TransferStatus status) {
        return Transfer.restore(1L, 1L, 2L, BigDecimal.valueOf(10000), "tx-001", status, "메모", null, null);
    }
}
