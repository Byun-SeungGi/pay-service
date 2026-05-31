CREATE TABLE wallet_creation_task (
  task_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '작업 ID',
  user_id BIGINT NOT NULL COMMENT '유저 ID',
  status VARCHAR(20) NOT NULL DEFAULT 'FAILED' COMMENT '작업 상태',
  created_at DATETIME(6) NOT NULL COMMENT '생성일시',
  updated_at DATETIME(6) NOT NULL COMMENT '수정일시',

  CONSTRAINT fk_wallet_creation_task_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='지갑 생성 작업';