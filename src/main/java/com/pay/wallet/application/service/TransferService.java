package com.pay.wallet.application.service;

import com.pay.wallet.application.port.in.TransferUseCase;
import com.pay.wallet.application.port.out.LedgerRepository;
import com.pay.wallet.application.port.out.TransferRepository;
import com.pay.wallet.application.port.out.WalletRepository;
import com.pay.wallet.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService implements TransferUseCase {

    private final TransferRepository transferRepository;
    private final WalletRepository walletRepository;
    private final LedgerRepository ledgerRepository;
    private final TransactionTemplate transactionTemplate;

    @Override
    public Transfer transfer(Long senderUserId, Long receiverUserId, BigDecimal amount, String transactionId, String memo) {

        String namespacedId = senderUserId + "::" + transactionId;

        // TX1 : 중복체크 + Transfer 생성
        // 멱등성 키로 중복 요청 걸러내고 Transfer를 REQUESTED 상태로 저장.
        Transfer saved;
        try {
            saved = transactionTemplate.execute(status -> {
                transferRepository.findByTransactionId(namespacedId)
                        .ifPresent(transfer -> {
                            throw new IllegalStateException("이미 처리된 송금 요청입니다.");
                        });
                return transferRepository.save(
                        Transfer.create(senderUserId, receiverUserId, amount, namespacedId, memo));
            });
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("이미 처리된 송금 요청입니다.");
        }

        // TX2 : 출금 + 입금 + 상태변경(COMPLETED) + 출금/입금 원장
        // 실패 시 전체 롤백 → Transfer를 FAILED로 변경
        try {
            return transactionTemplate.execute(status -> {
                Wallet senderWallet = walletRepository.findByUserIdForUpdate(senderUserId)
                        .orElseThrow(() -> new IllegalArgumentException("지갑이 존재하지 않습니다."));
                senderWallet = senderWallet.withDraw(amount);
                walletRepository.save(senderWallet);

                Wallet receiverWallet = walletRepository.findByUserIdForUpdate(receiverUserId)
                        .orElseThrow(() -> new IllegalArgumentException("지갑이 존재하지 않습니다."));
                receiverWallet = receiverWallet.deposit(amount);
                walletRepository.save(receiverWallet);

                Transfer completed = transferRepository.save(saved.complete());
                saveLedger(senderWallet, namespacedId, completed, TransactionType.DEBIT, amount, memo);
                saveLedger(receiverWallet, namespacedId, completed, TransactionType.CREDIT, amount, memo);
                return completed;
            });
        } catch (Exception e) {
            log.error("송금 실패 - transactionId : {}, 사유 : {}", transactionId, e.getMessage());
            return transactionTemplate.execute(status ->
                    transferRepository.save(saved.fail()));
        }
    }

    private void saveLedger(Wallet wallet, String namespacedId, Transfer transfer, TransactionType type, BigDecimal amount, String memo) {
        ledgerRepository.save(Ledger.create(wallet.getWalletId(), namespacedId, ReferenceType.REMITTANCE,
                transfer.getTransferId(), type, amount, wallet.getBalance(), memo));
    }
}
