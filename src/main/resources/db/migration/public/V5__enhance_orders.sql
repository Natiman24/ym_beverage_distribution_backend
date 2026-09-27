ALTER TABLE orders
    ADD COLUMN payment_status VARCHAR(50) NOT NULL DEFAULT 'UNPAID',
    ADD COLUMN payment_due_date DATE,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

UPDATE orders
SET payment_status = CASE
    WHEN status = 'RETURNED' THEN 'REFUNDED'
    WHEN is_paid = TRUE THEN 'PAID'
    ELSE 'UNPAID'
END;

ALTER TABLE orders DROP COLUMN is_paid;

CREATE TABLE order_status_histories
(
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    order_id        UUID        NOT NULL,
    previous_status VARCHAR(50),
    new_status      VARCHAR(50) NOT NULL,
    reason          TEXT,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    CONSTRAINT pk_order_status_histories PRIMARY KEY (id),
    CONSTRAINT fk_order_status_histories_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_status_histories_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_order_status_histories_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
);

INSERT INTO order_status_histories (order_id, previous_status, new_status, created_at, created_by)
SELECT id, NULL, status, COALESCE(updated_at, created_at, order_date), created_by
FROM orders;

CREATE INDEX idx_orders_driver_id ON orders (driver_id);
CREATE INDEX idx_orders_payment_method ON orders (payment_method);
CREATE INDEX idx_orders_payment_status ON orders (payment_status);
CREATE INDEX idx_orders_delivered_at ON orders (delivered_at);
CREATE INDEX idx_orders_payment_due_date ON orders (payment_due_date);
CREATE INDEX idx_orders_created_by ON orders (created_by);
CREATE INDEX idx_order_status_histories_order_id ON order_status_histories (order_id);
