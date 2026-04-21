DELETE FROM asset_rules;

INSERT INTO asset_rules (ticker, min_tick, lot_size, reference_price) VALUES
-- Blue Chips e Setores Diversos
('ITUB4', 0.01, 100, 32.50),
('BBDC4', 0.01, 100, 15.20),
('BBAS3', 0.01, 100, 28.10),
('SANB11', 0.01, 100, 29.40),
('PETR4', 0.01, 100, 38.50),
('PETR3', 0.01, 100, 41.20),
('VALE3', 0.01, 100, 112.00),
('ELET3', 0.01, 100, 45.30),
('CSNA3', 0.01, 100, 18.90),
('MGLU3', 0.01, 100, 2.15),
('ABEV3', 0.01, 100, 13.40),
('LREN3', 0.01, 100, 17.80),
('JBSS3', 0.01, 100, 24.60),
('WEGE3', 0.01, 100, 36.80),
('RENT3', 0.01, 100, 62.15),
('TOTS3', 0.01, 100, 30.20),
('AZUL4', 0.01, 100, 14.50),

-- Mercado Fracionário (Lote 1)
('WEGE3F', 0.01, 1, 36.80),
('PETR4F', 0.01, 1, 38.50),
('VALE3F', 0.01, 1, 112.00),
('ITUB4F', 0.01, 1, 32.50),
('ABEV3F', 0.01, 1, 13.40),
('MGLU3F', 0.01, 1, 2.15),
('BBDC4F', 0.01, 1, 15.20),
('BBAS3F', 0.01, 1, 28.10),

-- Casos de Borda (Edge Cases)
('AMER3', 0.01, 100, 0.45),
('SUZB3', 0.05, 100, 52.50),
('IVVB11', 0.01, 1, 285.00);