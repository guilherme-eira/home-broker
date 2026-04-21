CREATE TABLE positions(
    id BINARY(16) PRIMARY KEY,
    wallet_id BINARY(16) NOT NULL,
    ticker VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6)
)