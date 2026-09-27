-- ============================================================
-- V1: Full initial schema
-- ============================================================

-- Stores
CREATE TABLE stores
(
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(255),
    description VARCHAR(255),
    tin_number  VARCHAR(255),
    phone_number VARCHAR(255),
    email       VARCHAR(255),
    city        VARCHAR(255),
    sub_city    VARCHAR(255),
    address     VARCHAR(255),
    active      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  UUID,
    updated_by  UUID,
    CONSTRAINT pk_stores PRIMARY KEY (id),
    CONSTRAINT uq_stores_tin_number UNIQUE (tin_number)
);

-- Roles
CREATE TABLE roles
(
    id         UUID        NOT NULL DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uq_roles_name UNIQUE (name)
);

-- Role permissions (element collection)
CREATE TABLE role_permissions
(
    role_id    UUID        NOT NULL,
    permission VARCHAR(255) NOT NULL,
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Users
CREATE TABLE users
(
    id             UUID        NOT NULL DEFAULT gen_random_uuid(),
    first_name     VARCHAR(255) NOT NULL,
    last_name      VARCHAR(255),
    phone_number   VARCHAR(255) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    password       VARCHAR(255),
    is_active      BOOLEAN     NOT NULL DEFAULT FALSE,
    is_deactivated BOOLEAN     NOT NULL DEFAULT FALSE,
    store_id       UUID,
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP,
    created_by     UUID,
    updated_by     UUID,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_store FOREIGN KEY (store_id) REFERENCES stores (id),
    CONSTRAINT fk_users_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_users_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
);

-- Now that users exists, add FK on stores auditing columns
ALTER TABLE stores
    ADD CONSTRAINT fk_stores_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    ADD CONSTRAINT fk_stores_updated_by FOREIGN KEY (updated_by) REFERENCES users (id);

-- Roles auditing FKs
ALTER TABLE roles
    ADD CONSTRAINT fk_roles_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    ADD CONSTRAINT fk_roles_updated_by FOREIGN KEY (updated_by) REFERENCES users (id);

-- User roles (join table)
CREATE TABLE user_roles
(
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- User permissions (element collection)
CREATE TABLE user_permissions
(
    user_id    UUID        NOT NULL,
    permission VARCHAR(255) NOT NULL,
    CONSTRAINT fk_user_permissions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Drivers (joined inheritance child of users)
CREATE TABLE drivers
(
    user_id        UUID NOT NULL,
    license_number VARCHAR(255),
    CONSTRAINT pk_drivers PRIMARY KEY (user_id),
    CONSTRAINT fk_drivers_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_drivers_license_number UNIQUE (license_number)
);

-- OTPs
CREATE TABLE otps
(
    id              UUID    NOT NULL DEFAULT gen_random_uuid(),
    code            VARCHAR(255),
    number_of_tries INTEGER,
    user_id         UUID    NOT NULL,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    CONSTRAINT pk_otps PRIMARY KEY (id),
    CONSTRAINT fk_otps_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_otps_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_otps_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
);

-- Refresh tokens
CREATE TABLE refresh_tokens
(
    id          UUID NOT NULL DEFAULT gen_random_uuid(),
    token       VARCHAR(255),
    expiry_date BIGINT,
    user_id     UUID,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  UUID,
    updated_by  UUID,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT fk_refresh_tokens_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_refresh_tokens_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
);

-- Suppliers
CREATE TABLE suppliers
(
    id           UUID        NOT NULL DEFAULT gen_random_uuid(),
    name         VARCHAR(255) NOT NULL,
    description  VARCHAR(255),
    phone_number VARCHAR(255),
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP,
    created_by   UUID,
    updated_by   UUID,
    CONSTRAINT pk_suppliers PRIMARY KEY (id),
    CONSTRAINT uq_suppliers_name UNIQUE (name),
    CONSTRAINT fk_suppliers_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_suppliers_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
);

-- Brands
CREATE TABLE brands
(
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    active      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  UUID,
    updated_by  UUID,
    CONSTRAINT pk_brands PRIMARY KEY (id),
    CONSTRAINT uq_brands_name UNIQUE (name),
    CONSTRAINT fk_brands_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_brands_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
);

-- Product categories
CREATE TABLE product_categories
(
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    active      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  UUID,
    updated_by  UUID,
    CONSTRAINT pk_product_categories PRIMARY KEY (id),
    CONSTRAINT uq_product_categories_name UNIQUE (name),
    CONSTRAINT fk_product_categories_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_product_categories_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
);

-- Product units
CREATE TABLE product_units
(
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    active      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  UUID,
    updated_by  UUID,
    CONSTRAINT pk_product_units PRIMARY KEY (id),
    CONSTRAINT uq_product_units_name UNIQUE (name),
    CONSTRAINT fk_product_units_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_product_units_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
);

-- Volume units
CREATE TABLE volume_units
(
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    active      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  UUID,
    updated_by  UUID,
    CONSTRAINT pk_volume_units PRIMARY KEY (id),
    CONSTRAINT uq_volume_units_name UNIQUE (name),
    CONSTRAINT fk_volume_units_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_volume_units_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
);

-- Products
CREATE TABLE products
(
    id             UUID           NOT NULL DEFAULT gen_random_uuid(),
    name           VARCHAR(255)   NOT NULL,
    description    VARCHAR(255),
    brand_id       UUID,
    category_id    UUID           NOT NULL,
    unit_id        UUID           NOT NULL,
    volume         NUMERIC(19, 2),
    volume_unit_id UUID,
    units_per_pack INTEGER,
    quantity       INTEGER        NOT NULL DEFAULT 0,
    selling_price  NUMERIC(19, 2) NOT NULL,
    purchase_price NUMERIC(19, 2),
    active         BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP,
    created_by     UUID,
    updated_by     UUID,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT fk_products_brand FOREIGN KEY (brand_id) REFERENCES brands (id),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES product_categories (id),
    CONSTRAINT fk_products_unit FOREIGN KEY (unit_id) REFERENCES product_units (id),
    CONSTRAINT fk_products_volume_unit FOREIGN KEY (volume_unit_id) REFERENCES volume_units (id),
    CONSTRAINT fk_products_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_products_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
);

-- Product histories
CREATE TABLE product_histories
(
    id               UUID NOT NULL DEFAULT gen_random_uuid(),
    product_id       UUID NOT NULL,
    history_mode     VARCHAR(50),
    quantity_changed INTEGER,
    supplier_id      UUID,
    note             TEXT DEFAULT '',
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP,
    created_by       UUID,
    updated_by       UUID,
    CONSTRAINT pk_product_histories PRIMARY KEY (id),
    CONSTRAINT fk_product_histories_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_product_histories_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id),
    CONSTRAINT fk_product_histories_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_product_histories_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
);

-- Orders
CREATE TABLE orders
(
    id                     UUID           NOT NULL DEFAULT gen_random_uuid(),
    store_id               UUID           NOT NULL,
    status                 VARCHAR(50)    NOT NULL,
    order_date             TIMESTAMP      NOT NULL,
    expected_delivery_date TIMESTAMP,
    delivered_at           TIMESTAMP,
    total_amount           NUMERIC(19, 2) NOT NULL,
    delivery_address       VARCHAR(255),
    notes                  VARCHAR(255),
    payment_method         VARCHAR(50)    NOT NULL,
    is_paid                BOOLEAN        NOT NULL DEFAULT FALSE,
    driver_id              UUID,
    created_at             TIMESTAMP,
    updated_at             TIMESTAMP,
    created_by             UUID,
    updated_by             UUID,
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT fk_orders_store FOREIGN KEY (store_id) REFERENCES stores (id),
    CONSTRAINT fk_orders_driver FOREIGN KEY (driver_id) REFERENCES drivers (user_id),
    CONSTRAINT fk_orders_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_orders_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
);

-- Order items
CREATE TABLE order_items
(
    id          UUID           NOT NULL DEFAULT gen_random_uuid(),
    order_id    UUID           NOT NULL,
    product_id  UUID           NOT NULL,
    quantity    INTEGER        NOT NULL,
    unit_price  NUMERIC(19, 2) NOT NULL,
    total_price NUMERIC(19, 2) NOT NULL,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  UUID,
    updated_by  UUID,
    CONSTRAINT pk_order_items PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_order_items_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_order_items_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
);

-- Store transactions
CREATE TABLE store_transactions
(
    id               UUID           NOT NULL DEFAULT gen_random_uuid(),
    store_id         UUID           NOT NULL,
    transaction_type VARCHAR(50)    NOT NULL,
    amount           NUMERIC(19, 2) NOT NULL,
    order_id         UUID,
    note             TEXT,
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP,
    created_by       UUID,
    updated_by       UUID,
    CONSTRAINT pk_store_transactions PRIMARY KEY (id),
    CONSTRAINT fk_store_transactions_store FOREIGN KEY (store_id) REFERENCES stores (id),
    CONSTRAINT fk_store_transactions_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_store_transactions_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_store_transactions_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
);
