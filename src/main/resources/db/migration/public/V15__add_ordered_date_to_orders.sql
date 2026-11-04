ALTER TABLE orders
    ADD COLUMN ordered_date TIMESTAMP;

UPDATE orders o
SET ordered_date = COALESCE(
    (
        SELECT MIN(h.created_at)
        FROM order_status_histories h
        WHERE h.order_id = o.id
          AND h.new_status = 'SUBMITTED'
    ),
    o.order_date
)
WHERE o.status <> 'DRAFT';

CREATE INDEX idx_orders_ordered_date ON orders (ordered_date);
