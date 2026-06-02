ALTER TABLE investments.holdings DROP COLUMN IF EXISTS bank_account_id;
ALTER TABLE investments.holdings DROP COLUMN IF EXISTS bank_id;
ALTER TABLE investments.holdings ADD COLUMN account_cbu VARCHAR(22);
CREATE INDEX IF NOT EXISTS idx_holdings_account_cbu ON investments.holdings (account_cbu);
