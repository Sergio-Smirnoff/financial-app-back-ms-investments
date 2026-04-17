-- ═══════════════════════════════════════════════════════════════════
-- V4: Add OHLC/volume columns to asset_prices; create asset_price_history
-- ═══════════════════════════════════════════════════════════════════

-- Extend current price snapshot with OHLC + volume context
ALTER TABLE investments.asset_prices
    ADD COLUMN IF NOT EXISTS open_price      NUMERIC(18, 6),
    ADD COLUMN IF NOT EXISTS high_price      NUMERIC(18, 6),
    ADD COLUMN IF NOT EXISTS low_price       NUMERIC(18, 6),
    ADD COLUMN IF NOT EXISTS volume          NUMERIC(18, 2),
    ADD COLUMN IF NOT EXISTS daily_variation NUMERIC(8, 4);

-- Historical price snapshots (one row per refresh per ticker)
CREATE TABLE investments.asset_price_history (
    id               BIGSERIAL      PRIMARY KEY,
    ticker           VARCHAR(20)    NOT NULL,
    asset_type       VARCHAR(20)    NOT NULL,
    last_price       NUMERIC(18, 6) NOT NULL,
    open_price       NUMERIC(18, 6),
    high_price       NUMERIC(18, 6),
    low_price        NUMERIC(18, 6),
    volume           NUMERIC(18, 2),
    daily_variation  NUMERIC(8, 4),
    currency         VARCHAR(3)     NOT NULL,
    priced_at        TIMESTAMP      NOT NULL
);

CREATE INDEX idx_aph_ticker_priced_at
    ON investments.asset_price_history (ticker, priced_at);
