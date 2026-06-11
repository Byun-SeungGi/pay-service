ALTER TABLE wallet_creation_task RENAME TO user_created_event_outbox;

ALTER TABLE user_created_event_outbox CHANGE task_id event_id BIGINT AUTO_INCREMENT COMMENT '이벤트 ID';

ALTER TABLE user_created_event_outbox MODIFY status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '이벤트 상태';
