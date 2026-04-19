-- ═══════════════════════════════════════════════════════════════════
-- V5: Link holdings to bank accounts
-- ═══════════════════════════════════════════════════════════════════

ALTER TABLE investments.holdings
ADD COLUMN bank_account_id BIGINT NULL;

CREATE INDEX idx_holdings_bank_account_id ON investments.holdings (bank_account_id);
