CREATE TABLE wallets(
    id BINARY(16) PRIMARY KEY,
    owner_id BINARY(16) NOT NULL UNIQUE,
    available_balance DECIMAL(19,2) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    CONSTRAINT fk_wallet_user FOREIGN KEY (owner_id) REFERENCES investors(id)
)