CREATE TABLE investments.broker_fee_schedules (
    id             BIGSERIAL PRIMARY KEY,
    bank_number    VARCHAR(3)  NOT NULL,
    asset_type     VARCHAR(20),
    buy_fee_pct    NUMERIC(5,3),
    sell_fee_pct   NUMERIC(5,3),
    minimum_fee    NUMERIC(15,2),
    market_fee_pct NUMERIC(5,3),
    iva_treatment  VARCHAR(10) NOT NULL DEFAULT 'SEPARATE',
    currency       CHAR(3)     NOT NULL DEFAULT 'ARS',
    UNIQUE NULLS NOT DISTINCT (bank_number, asset_type)
);
