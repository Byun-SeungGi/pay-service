package com.pay.wallet.application.port.in;

import com.pay.wallet.domain.Transfer;

import java.math.BigDecimal;

public interface TransferUseCase {

    Transfer transfer(Long senderUserId, Long receiverUserId, BigDecimal amount, String transactionId, String memo);
}
