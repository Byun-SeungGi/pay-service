-- V1__init.sql
CREATE TABLE users (
   user_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '유저 ID(내부용)',
   external_id BINARY(16) NOT NULL COMMENT '외부 식별자 (UUID)',
   email VARCHAR(100) NOT NULL COMMENT '이메일 (로그인 ID)',
   password VARCHAR(255) NOT NULL COMMENT '비밀번호 (암호화)',
   phone_number VARCHAR(20) NOT NULL COMMENT '휴대폰 번호',
   status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '계정 상태',
   created_at DATETIME(6) NOT NULL COMMENT '생성일시',
   updated_at DATETIME(6) NOT NULL COMMENT '수정일시',
   withdrawn_at DATETIME(6) COMMENT '탈퇴일시',

   CONSTRAINT uk_users_external_id UNIQUE (external_id),
   CONSTRAINT uk_users_email UNIQUE (email),
   CONSTRAINT uk_users_phone_number UNIQUE (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='회원';