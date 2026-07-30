CREATE TABLE investments.market_indices (
    code       VARCHAR(20) PRIMARY KEY,
    value      NUMERIC(14,2) NOT NULL,
    variation  NUMERIC(8,4),
    updated_at TIMESTAMP     NOT NULL DEFAULT now()
);
