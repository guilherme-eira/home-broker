CREATE TABLE orders(
    id BINARY(16) PRIMARY KEY,
    investor_id BINARY(16) NOT NULL,
    ticker VARCHAR(100) NOT NULL,
    total_quantity INT NOT NULL,
    price_limit DECIMAL(19, 2) NOT NULL,
    filled_quantity INT,
    average_price DECIMAL(19, 2),
    type VARCHAR(100) NOT NULL,
    side VARCHAR(100) NOT NULL,
    status VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6)
)