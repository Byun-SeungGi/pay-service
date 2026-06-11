CREATE TABLE transfer (
    transfer_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '송금ID',
    sender_user_id BIGINT NOT NULL COMMENT '보내는 사람',
    receiver_user_id BIGINT NOT NULL COMMENT '받는 사람',
    amount DECIMAL(19, 4) NOT NULL COMMENT '송금 금액',
    transaction_id VARCHAR(60) NOT NULL COMMENT '멱등성키(userId::uuidv7)',
    status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED' COMMENT '송금 상태',
    memo VARCHAR(100) COMMENT '송금 메모',
    created_at DATETIME(6) NOT NULL DEFAULT NOW(6) COMMENT '송금요청일시',
    updated_at DATETIME(6) NOT NULL DEFAULT NOW(6) COMMENT '상태변경일시',

    CONSTRAINT uk_transfer_transaction_id UNIQUE (transaction_id),
    INDEX idx_transfer_sender_date (sender_user_id, created_at),
    INDEX idx_transfer_receiver_date (receiver_user_id, created_at),
    INDEX idx_transfer_status (status),
    CONSTRAINT fk_transfer_sender FOREIGN KEY (sender_user_id) REFERENCES users (user_id),
    CONSTRAINT fk_transfer_receiver FOREIGN KEY (receiver_user_id) REFERENCES users (user_id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='송금';
