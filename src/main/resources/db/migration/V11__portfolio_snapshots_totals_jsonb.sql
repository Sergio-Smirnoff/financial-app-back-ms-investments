-- Migrate portfolio_snapshots from per-currency columns to a JSONB map.
-- Backfills ARS/USD into a single JSONB column, then drops the legacy columns.

ALTER TABLE investments.portfolio_snapshots
    ADD COLUMN totals JSONB;

UPDATE investments.portfolio_snapshots
SET totals = jsonb_build_object(
    'ARS', total_value_ars,
    'USD', total_value_usd
);

ALTER TABLE investments.portfolio_snapshots
    ALTER COLUMN totals SET NOT NULL;

ALTER TABLE investments.portfolio_snapshots
    DROP COLUMN total_value_ars,
    DROP COLUMN total_value_usd;
