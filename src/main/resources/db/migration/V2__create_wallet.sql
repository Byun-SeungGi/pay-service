CREATE TABLE wallet (
    wallet_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '지갑 ID(내부용)',
    external_id BINARY(16) NOT NULL COMMENT '외부 노출용 (UUID)',
    user_id BIGINT NOT NULL COMMENT '유저 ID',
    balance DECIMAL(19, 4) NOT NULL DEFAULT 0 COMMENT '잔액',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 락 버전',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '지갑 상태',
    created_at DATETIME(6) NOT NULL COMMENT '생성일시',
    updated_at DATETIME(6) NOT NULL COMMENT '수정일시',

    CONSTRAINT uk_wallet_external_id UNIQUE (external_id),
    CONSTRAINT uk_wallet_user_id UNIQUE (user_id),
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='지갑';