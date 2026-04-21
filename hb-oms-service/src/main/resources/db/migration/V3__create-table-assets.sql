CREATE TABLE asset_rules(
    ticker VARCHAR(100) PRIMARY KEY,
    min_tick DECIMAL(19, 2) NOT NULL,
    lot_size INT NOT NULL,
    reference_price DECIMAL(19, 2) NOT NULL
)