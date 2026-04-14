-- ═══════════════════════════════════════════════════════════════════
-- V2: Add notification threshold fields to holdings
-- ═══════════════════════════════════════════════════════════════════

ALTER TABLE investments.holdings ADD COLUMN notify_gain_threshold_pct NUMERIC(5,2);
ALTER TABLE investments.holdings ADD COLUMN notify_loss_threshold_pct NUMERIC(5,2);
ALTER TABLE investments.holdings ADD COLUMN last_gain_notified_at TIMESTAMP;
ALTER TABLE investments.holdings ADD COLUMN last_loss_notified_at TIMESTAMP;
