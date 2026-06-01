CREATE TABLE investments.refresh_jobs (
    id            BIGSERIAL PRIMARY KEY,
    status        VARCHAR(20)  NOT NULL,
    all_tickers   TEXT         NOT NULL,
    last_success_index INT      NOT NULL DEFAULT -1,
    failure_reason TEXT,
    started_at    TIMESTAMP    NOT NULL,
    completed_at  TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL
);
