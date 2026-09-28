ALTER TABLE order_items
    ADD COLUMN unit_cost NUMERIC(19, 2);

-- Preserve the best cost information available for orders created before
-- unit_cost was captured on each order item.
UPDATE order_items oi
SET unit_cost = p.purchase_price
FROM products p
WHERE oi.product_id = p.id;
