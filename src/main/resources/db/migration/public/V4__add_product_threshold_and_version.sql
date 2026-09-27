ALTER TABLE products
    ADD COLUMN threshold_quantity INTEGER NOT NULL DEFAULT 10,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE products
    ADD CONSTRAINT ck_products_threshold_quantity_non_negative
        CHECK (threshold_quantity >= 0);
