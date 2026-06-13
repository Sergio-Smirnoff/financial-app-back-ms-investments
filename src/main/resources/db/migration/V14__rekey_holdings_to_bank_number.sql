DELETE FROM investments.holdings WHERE account_cbu IS NULL OR length(account_cbu) < 3;

ALTER TABLE investments.holdings ADD COLUMN bank_number varchar(3);
UPDATE investments.holdings SET bank_number = substr(account_cbu, 1, 3);
ALTER TABLE investments.holdings ALTER COLUMN bank_number SET NOT NULL;

DROP INDEX IF EXISTS investments.idx_holdings_account_cbu;
CREATE INDEX idx_holdings_user_bank_currency
    ON investments.holdings (user_id, bank_number, currency);

ALTER TABLE investments.holdings DROP COLUMN account_cbu;
