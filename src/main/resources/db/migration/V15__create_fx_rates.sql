CREATE TABLE investments.fx_rates (
    id         BIGSERIAL PRIMARY KEY,
    rate_date  DATE          NOT NULL,
    fx_view    VARCHAR(10)   NOT NULL,
    buy        NUMERIC(12,4) NOT NULL,
    sell       NUMERIC(12,4) NOT NULL,
    source     VARCHAR(20)   NOT NULL,
    created_at TIMESTAMP     NOT NULL DEFAULT now(),
    UNIQUE (rate_date, fx_view)
);
