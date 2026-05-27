-- V1__init_order_tables.sql

CREATE TABLE orders (
    order_no VARCHAR(255) PRIMARY KEY,
    customer VARCHAR(255) NOT NULL,
    pricebook VARCHAR(255) NOT NULL,
    warehouse VARCHAR(255) NOT NULL,
    total_price DECIMAL(19, 2),
    order_status VARCHAR(50) NOT NULL,
    order_type VARCHAR(50) NOT NULL
);

CREATE TABLE order_lines (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(255) NOT NULL,
    product VARCHAR(255) NOT NULL,
    qty INT NOT NULL,
    price DECIMAL(19, 2) NOT NULL,
    CONSTRAINT fk_order_no FOREIGN KEY (order_no) REFERENCES orders(order_no) ON DELETE CASCADE
);

CREATE TABLE back_orders (
    id BIGSERIAL PRIMARY KEY,
    customer VARCHAR(255) NOT NULL,
    pricebook VARCHAR(255) NOT NULL,
    product VARCHAR(255) NOT NULL,
    warehouse VARCHAR(255) NOT NULL,
    backorder_qty INT NOT NULL,
    customer_response VARCHAR(50) NOT NULL,
    original_order_no VARCHAR(255) NOT NULL
);