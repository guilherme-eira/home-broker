CREATE TABLE trades (
    id BINARY(16) PRIMARY KEY,
    bid_order_id BINARY(16) NOT NULL,
    ask_order_id BINARY(16) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(19, 8) NOT NULL,
    executed_at DATETIME(6) NOT NULL
)