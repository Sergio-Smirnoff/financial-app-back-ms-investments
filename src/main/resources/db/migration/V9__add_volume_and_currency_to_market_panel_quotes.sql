ALTER TABLE investments.market_panel_quotes
    ADD COLUMN IF NOT EXISTS volume DECIMAL(18, 2),
    ADD COLUMN IF NOT EXISTS currency VARCHAR(3);
