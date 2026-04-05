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

        // TX2 : 출금 + 상태변경(WITHDRAWN) + 출금 원장
        // withdraw와 save(withdrawn)를 하나의 TX로 묶어 돈은 빠졌는데 상태는 REQUESTED인 상황을 제거
        // 실패 시 TX2 전체 롤백 -> 지갑 원래대로 복구
        Transfer withdrawn;
        try {
            withdrawn = transactionTemplate.execute(status -> {
                Wallet senderWallet = walletRepository.findByUserIdForUpdate(senderUserId)
                        .orElseThrow(() -> new IllegalArgumentException("지갑이 존재하지 않습니다."));
                senderWallet = senderWallet.withDraw(amount);
                walletRepository.save(senderWallet);
                Transfer w = transferRepository.save(saved.withDraw());
                saveLedger(senderWallet, namespacedId, w, TransactionType.DEBIT, amount, memo);
                return w;
            });
        } catch(Exception e) {
            log.error("출금 실패 - transactionId : {}, 사유 : {}", transactionId, e.getMessage());
            return transactionTemplate.execute(status->
                    transferRepository.save(saved.fail()));
        }

        // TX3 : 입금 + 상태변경(COMPLETED) + 입금 원장
        // 실패 시 보상 트랜잭션(refund) 실행
        try {
            return transactionTemplate.execute(status -> {
                Wallet receiverWallet = walletRepository.findByUserIdForUpdate(receiverUserId)
                        .orElseThrow(() -> new IllegalArgumentException("지갑이 존재하지 않습니다."));
                receiverWallet = receiverWallet.deposit(amount);
                walletRepository.save(receiverWallet);
                Transfer r = transferRepository.save(withdrawn.complete());
                saveLedger(receiverWallet, namespacedId, r, TransactionType.CREDIT, amount, memo);
                return r;
            });
        } catch (Exception e) {
            log.error("입금 실패 - transactionId : {}, 사유 : {}", transactionId, e.getMessage());
            return refund(withdrawn.getTransferId());
        }
    }


    /*
        보상 트랜잭션 : 환불

        - 입금 실패 시 TransferService에서 직접 호출
        - 서버 장애 시 RecoveryScheduler가 호출

        SELECT FOR UPDATE로 락을 걸어서 원래 요청과 스케줄러가 동시에 환불하는 상황 방지
    */
    public Transfer refund(Long transferId) {
        try {
            return transactionTemplate.execute(status -> {
               Transfer transfer = transferRepository.findByIdForUpdate(transferId)
                    .orElseThrow(() -> new IllegalArgumentException("송금 건을 찾을 수 없습니다."));

               if(transfer.getStatus() != TransferStatus.WITHDRAWN) {
                   return transfer;
               }

               Wallet refundedWallet = walletRepository.findByUserIdForUpdate(transfer.getSenderUserId())
                       .orElseThrow(() -> new IllegalArgumentException("지갑이 존재하지 않습니다."));
               refundedWallet = refundedWallet.deposit(transfer.getAmount());
               walletRepository.save(refundedWallet);
               Transfer refunded = transferRepository.save(transfer.refund());
               saveLedger(refundedWallet, transfer.getTransactionId(), refunded, TransactionType.CREDIT, transfer.getAmount(), transfer.getMemo());
               return refunded;
            });
        } catch (Exception e) {
            log.error("환불 실패 - transferId: {}, 사유: {}", transferId, e.getMessage());
            return transactionTemplate.execute(status-> {
                Transfer transfer = transferRepository.findByIdForUpdate(transferId)
                    .orElseThrow();
                return transferRepository.save(transfer.refundFail());
            });
        }
    }

    private void saveLedger(Wallet wallet, String namespacedId, Transfer transfer, TransactionType type, BigDecimal amount, String memo) {
        ledgerRepository.save(Ledger.create(wallet.getWalletId(), namespacedId, ReferenceType.REMITTANCE,
                transfer.getTransferId(), type, amount, wallet.getBalance(), memo));
    }
}
