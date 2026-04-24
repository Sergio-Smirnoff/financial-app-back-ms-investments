CREATE TABLE portfolio_snapshots (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    snapshot_date DATE NOT NULL,
    total_value_ars DECIMAL(19, 4) NOT NULL,
    total_value_usd DECIMAL(19, 4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, snapshot_date)
);

CREATE INDEX idx_portfolio_snapshots_user_date ON portfolio_snapshots(user_id, snapshot_date);
