ALTER TABLE payments
    DROP COLUMN order_id,
    ADD COLUMN order_id BIGINT,
    ADD COLUMN razorpay_order_id VARCHAR(255),
    ADD COLUMN razorpay_payment_id VARCHAR(255),
    ADD COLUMN created_at TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP;

UPDATE payments SET created_at = now(), updated_at = now() WHERE created_at IS NULL;

ALTER TABLE payments
    ALTER COLUMN order_id SET NOT NULL,
    ALTER COLUMN razorpay_order_id SET NOT NULL,
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL,
    ADD CONSTRAINT uq_payments_order_id UNIQUE (order_id),
    ADD CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id);
