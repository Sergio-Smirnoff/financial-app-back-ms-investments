# ms-investments — messaging and jobs

CloudEvents 1.0, Kafka binary mode, via `commons-messaging`. Topic name = `ce_type`. Outbox,
`OutboxRelay` and DLT conventions: parent `.ai/references/ARCHITECTURE.md` — not repeated here.

## Published

| ce_type / topic | when emitted | payload fields |
|---|---|---|
| `investments.threshold.breached` | `EvaluateThresholdsUseCase` detects holding gain/loss P&L threshold breach | userId, holdingId, ticker, assetType, thresholdType (GAIN/LOSS), breachPct, currentPrice |

Emitted via transactional outbox (`outbox_event`) and published by `OutboxRelay` to ms-notifications.

## Consumed

ms-investments is REST + IOL broker API driven and **consumes no Kafka events**.

## Scheduled jobs

| Job | Trigger / Cron | What it does |
|---|---|---|
| `PriceRefreshScheduler.refreshPrices` | `iol.price-refresh-cron` (weekdays 10–17 ARS) | Refreshes OHLC prices via IOL API, updates history, and evaluates threshold alerts |
| `MarketDiscoveryScheduler.syncMarketPanel` | `iol.discovery-refresh-rate` (default 15m) | Syncs market discovery panel quotes and indices from IOL API |
| `PortfolioSnapshotScheduler.captureDailySnapshots` | `0 0 0 * * *` (daily midnight) | Captures daily EOD portfolio totals (JSONB by currency) for evolution charts |

## Outbound calls

| Target service | Endpoint / API | Why |
|---|---|---|
| IOL Broker API | OAuth2 `/token`, CotizacionDetalle, seriehistorica, panel | Fetches live market prices, historical series, and market discovery quotes |
| ms-finances | `POST /api/v1/finances/transactions` (`FinancesGatewayImpl`) | Records buy/sell cash leg transactions using broker sentinel CBU (`INVEST_BROKER_CBU_*`) |
