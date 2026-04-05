CREATE TABLE investments.holdings
(
    id                 BIGSERIAL     PRIMARY KEY,
    user_id            BIGINT        NOT NULL,
    ticker             VARCHAR(20)   NOT NULL,
    name               VARCHAR(100)  NOT NULL,
    asset_type         VARCHAR(20)   NOT NULL,
    quantity           NUMERIC(18,6) NOT NULL,
    avg_purchase_price NUMERIC(18,6) NOT NULL,
    currency           VARCHAR(3)    NOT NULL,
    created_at         TIMESTAMP     NOT NULL,
    updated_at         TIMESTAMP     NOT NULL
);

CREATE INDEX idx_holdings_user_id ON investments.holdings (user_id);
CREATE INDEX idx_holdings_ticker ON investments.holdings (ticker);

CREATE TABLE investments.asset_prices
(
    id         BIGSERIAL     PRIMARY KEY,
    ticker     VARCHAR(20)   NOT NULL UNIQUE,
    asset_type VARCHAR(20)   NOT NULL,
    last_price NUMERIC(18,6) NOT NULL,
    currency   VARCHAR(3)    NOT NULL,
    priced_at  TIMESTAMP     NOT NULL,
    updated_at TIMESTAMP     NOT NULL
);
