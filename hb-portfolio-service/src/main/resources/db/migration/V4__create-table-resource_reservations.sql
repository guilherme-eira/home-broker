CREATE TABLE resource_reservations (
    id BINARY(16) PRIMARY KEY,
    order_id BINARY(16) NOT NULL,
    wallet_id BINARY(16) NOT NULL,
    type VARCHAR(20) NOT NULL,
    ticker VARCHAR(20),
    total_volume DECIMAL(19, 2) NOT NULL,
    settled_volume DECIMAL(19, 2) NOT NULL,
    remaining_volume DECIMAL(19, 2) NOT NULL,
    status VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6)
);