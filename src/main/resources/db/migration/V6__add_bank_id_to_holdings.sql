-- Add bank_id to holdings to enable bank-level encapsulation
ALTER TABLE investments.holdings ADD COLUMN bank_id BIGINT;

-- Populate bank_id from existing bank_account_id if possible (requires cross-schema query or assuming it's done via app logic)
-- For now, we just add the column. In a real migration we might need more complex logic.

CREATE INDEX idx_holdings_bank_id ON investments.holdings (bank_id);
