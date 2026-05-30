-- ledger
ALTER TABLE ledger ADD UNIQUE INDEX uk_ledger_transaction_id (transaction_id);
ALTER TABLE ledger ADD INDEX idx_ledger_wallet_date (wallet_id, created_at);
ALTER TABLE ledger ADD INDEX idx_ledger_reference (reference_type, reference_id);
ALTER TABLE ledger ADD CONSTRAINT fk_wallet_id FOREIGN KEY (wallet_id) REFERENCES wallet (wallet_id);

-- payment
ALTER TABLE payment ADD UNIQUE INDEX uk_payment_payment_key (payment_key);
ALTER TABLE payment ADD UNIQUE INDEX uk_payment_order_id (order_id);
ALTER TABLE payment ADD INDEX idx_payment_user_date (user_id, created_at);
ALTER TABLE payment ADD INDEX idx_payment_status (status);
ALTER TABLE payment ADD CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users (user_id);

-- transfer
ALTER TABLE transfer ADD INDEX idx_transfer_sender_date (sender_user_id, created_at);
ALTER TABLE transfer ADD INDEX idx_transfer_receiver_date (receiver_user_id, created_at);
ALTER TABLE transfer ADD INDEX idx_transfer_status (status);
ALTER TABLE transfer ADD CONSTRAINT fk_transfer_sender FOREIGN KEY (sender_user_id) REFERENCES users (user_id);
ALTER TABLE transfer ADD CONSTRAINT fk_transfer_receiver FOREIGN KEY (receiver_user_id) REFERENCES users (user_id);

-- wallet (V2 fk_wallet_user 누락)
ALTER TABLE wallet ADD CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users (user_id);

-- user_created_event_outbox
ALTER TABLE user_created_event_outbox ADD CONSTRAINT fk_user_created_event_outbox_user FOREIGN KEY (user_id) REFERENCES users (user_id);
