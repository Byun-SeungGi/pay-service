CREATE TABLE ledger (
    ledger_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '원장번호',
    wallet_id BIGINT NOT NULL COMMENT '지갑번호',
    transaction_id VARCHAR(36) NOT NULL COMMENT '멱등성키',
    reference_type VARCHAR(20) NOT NULL COMMENT '출처타입',
    reference_id BIGINT NOT NULL COMMENT '출처번호(주문번호, 송금ID)',
    transaction_type VARCHAR(20) NOT NULL COMMENT '변동유형(credit:입금, debit:송금)',
    amount DECIMAL(19, 4) NOT NULL COMMENT '변동 금액',
    balance_snapshot DECIMAL(19, 4) NOT NULL COMMENT '거래 직후 지갑 잔액',
    memo VARCHAR(100) COMMENT '거래메모',
    created_at DATETIME(6) NOT NULL DEFAULT NOW(6) COMMENT '거래발생일시',

    INDEX idx_ledger_wallet_date (wallet_id, created_at),
    CONSTRAINT uk_ledger_transaction_id UNIQUE (transaction_id),
    INDEX idx_ledger_reference (reference_type, reference_id),
    CONSTRAINT fk_wallet_id FOREIGN KEY (wallet_id) REFERENCES wallet (wallet_id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='원장';