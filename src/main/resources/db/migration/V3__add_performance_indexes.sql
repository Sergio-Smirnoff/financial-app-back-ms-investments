-- ═══════════════════════════════════════════════════════════════════
-- V3: Add performance indexes for holdings and asset_prices
-- ═══════════════════════════════════════════════════════════════════

-- Composite index for user+ticker lookups
CREATE INDEX IF NOT EXISTS idx_holdings_user_ticker
    ON investments.holdings (user_id, ticker);

-- Partial index for threshold-check queries (only rows that need checking)
CREATE INDEX IF NOT EXISTS idx_holdings_notify
    ON investments.holdings (user_id)
    WHERE notify_gain_threshold_pct IS NOT NULL
       OR notify_loss_threshold_pct IS NOT NULL;

-- Asset prices lookup by ticker
CREATE INDEX IF NOT EXISTS idx_asset_prices_ticker
    ON investments.asset_prices (ticker);
