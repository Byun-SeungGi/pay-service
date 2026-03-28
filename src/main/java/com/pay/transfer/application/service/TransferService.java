package com.pay.transfer.application.service;

import com.pay.transfer.application.port.in.TransferUseCase;
import com.pay.transfer.application.port.out.TransferRepository;
import com.pay.transfer.domain.Transfer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService implements TransferUseCase {

    private final TransferRepository transferRepository;

    @Override
    public Transfer transfer(Long senderUserId, Long receiverUserId, BigDecimal amount, String transactionId, String memo) {

        transferRepository.findByTransactionId(transactionId)
            .ifPresent(transfer -> {
                throw new IllegalStateException("이미 처리된 송금 요청입니다.");
        });

        Transfer transfer = Transfer.create(senderUserId, receiverUserId, amount, transactionId, memo);

        return transferRepository.save(transfer);
    }
}
