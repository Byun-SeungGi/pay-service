package com.pay.transfer.application.service;

import com.pay.transfer.application.port.in.TransferUseCase;
import com.pay.transfer.application.port.out.TransferLedgerPort;
import com.pay.transfer.application.port.out.TransferRepository;
import com.pay.transfer.application.port.out.TransferWalletPort;
import com.pay.transfer.domain.Transfer;
import com.pay.wallet.domain.Ledger;
import com.pay.wallet.domain.ReferenceType;
import com.pay.wallet.domain.TransactionType;
import com.pay.wallet.domain.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService implements TransferUseCase {

    private final TransferRepository transferRepository;
    private final TransferWalletPort transferWalletPort;
    private final TransferLedgerPort transferLedgerPort;

    @Override
    @Transactional
    public Transfer transfer(Long senderUserId, Long receiverUserId, BigDecimal amount, String transactionId, String memo) {

        transferRepository.findByTransactionId(transactionId)
            .ifPresent(transfer -> {
                throw new IllegalStateException("이미 처리된 송금 요청입니다.");
        });

        // Requested
        Transfer transfer = Transfer.create(senderUserId, receiverUserId, amount, transactionId, memo);
        Transfer savedTransfer = transferRepository.save(transfer);

        // Withdrawn
        Wallet senderWallet;
        try {
            senderWallet = transferWalletPort.withdraw(senderUserId, amount);
        } catch (Exception e) {
            log.error("출금 실패 - transactionId : {}, 사유 : {}", transactionId, e.getMessage());
            Transfer failedTransfer = savedTransfer.fail();
            return transferRepository.save(failedTransfer);
        }

        Transfer withdrawnTransfer = savedTransfer.withDraw();
        Transfer updatedTransfer = transferRepository.save(withdrawnTransfer);

        transferLedgerPort.save(Ledger.create(
            senderWallet.getWalletId(),
            transactionId,
            ReferenceType.REMITTANCE,
            updatedTransfer.getTransferId(),
            TransactionType.DEBIT,
            amount,
            senderWallet.getBalance(),
            memo
        ));

        // Completed
        Wallet receiverWallet;
        try {
            receiverWallet = transferWalletPort.deposit(receiverUserId, amount);
        } catch (Exception e) {
            log.error("입금 실패, 환불 진행 - transactionId: {}, 사유 : {}", transactionId, e.getMessage());
            transferWalletPort.deposit(senderUserId, amount);
            Transfer refundedTransfer = updatedTransfer.refund();
            return transferRepository.save(refundedTransfer);
        }

        Transfer completedTransfer = updatedTransfer.complete();
        Transfer finalTransfer = transferRepository.save(completedTransfer);

        transferLedgerPort.save(Ledger.create(
            receiverWallet.getWalletId(),
            transactionId,
            ReferenceType.REMITTANCE,
            finalTransfer.getTransferId(),
            TransactionType.CREDIT,
            amount,
            receiverWallet.getBalance(),  // 입금 후 잔액
            memo
        ));

        return finalTransfer;
    }
}
