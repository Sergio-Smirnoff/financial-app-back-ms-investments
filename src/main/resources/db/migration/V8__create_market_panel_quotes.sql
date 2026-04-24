CREATE TABLE investments.market_panel_quotes (
    ticker VARCHAR(20) PRIMARY KEY,
    last_price DECIMAL(19, 4) NOT NULL,
    variation DECIMAL(19, 4),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
