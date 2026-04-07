CREATE TABLE payment (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '결제ID',
    payment_key VARCHAR(200) NOT NULL COMMENT '토스페이먼츠 결제 고유키',
    order_id VARCHAR(64) NOT NULL COMMENT '주문번호(멱등성키)',
    user_id BIGINT NOT NULL COMMENT '결제한 사용자',
    amount DECIMAL(19, 4) NOT NULL COMMENT '결제 금액',
    cancel_amount DECIMAL(19, 4) NOT NULL DEFAULT 0 COMMENT '누적 취소 금액',
    status VARCHAR(20) NOT NULL DEFAULT 'READY' COMMENT '결제 상태',
    created_at DATETIME(6) NOT NULL DEFAULT NOW(6) COMMENT '결제생성일시',
    updated_at DATETIME(6) NOT NULL DEFAULT NOW(6) COMMENT '상태변경일시',

    CONSTRAINT uk_payment_payment_key UNIQUE (payment_key),
    CONSTRAINT uk_payment_order_id UNIQUE (order_id),
    INDEX idx_payment_user_date (user_id, created_at),
    INDEX idx_payment_status (status),
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users (user_id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='결제';
