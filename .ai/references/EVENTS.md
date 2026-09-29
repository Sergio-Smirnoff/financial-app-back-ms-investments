# ms-investments — messaging and jobs

CloudEvents 1.0, Kafka binary mode, via `commons-messaging`. Topic name = `ce_type`. Outbox,
`OutboxRelay` and DLT conventions: parent `.ai/references/ARCHITECTURE.md` — not repeated here.

## Published

| ce_type / topic | when emitted | payload fields |
|---|---|---|
| `investments.threshold.breached` | `EvaluateThresholdsUseCase` detects holding gain/loss P&L threshold breach | userId, holdingId, ticker, assetType, thresholdType (GAIN/LOSS), breachPct, currentPrice |

For `BOND` holdings the threshold check compares the per-100 market value with the per-unit cost, and `currentPrice` in the event is the price per VN (`AssetType.unitPrice`), not the raw per-100 quote — ms-notifications prints it in the alert text.

Emitted via transactional outbox (`outbox_event`) and published by `OutboxRelay` to ms-notifications.

## Consumed

ms-investments is REST + IOL broker API driven and **consumes no Kafka events**.

## Scheduled jobs

| Job | Trigger / Cron | What it does |
|---|---|---|
| `PriceRefreshScheduler.refreshPrices` | `iol.price-refresh-cron` (weekdays 10–17 ARS) | Refreshes OHLC prices via IOL API, updates history, and evaluates threshold alerts |
| `MarketDiscoveryScheduler.syncMarketPanel` | `iol.discovery-refresh-rate` (default 15m) | Syncs market discovery panel quotes and indices from IOL API |
| `PortfolioSnapshotScheduler.captureSnapshots` | `0 0 0 * * *` in `investments.zone` (ART midnight) | Ensures each holder's snapshot (JSONB totals by currency, for evolution charts) for the ART day through the same per-user body as `POST /portfolio/snapshot/ensure-today`; users that already have today's row are skipped, never an error. `CapturePortfolioSnapshotUseCase.execute()` returns `SnapshotCaptureResult(attempted, failed)`; when `failed > 0` the scheduler logs `ERROR "Portfolio snapshot capture failed for X of Y users"` |

## Outbound calls

| Target service | Endpoint / API | Why |
|---|---|---|
| IOL Broker API | OAuth2 `/token`, CotizacionDetalle, seriehistorica, panel | Fetches live market prices, historical series, and market discovery quotes |
| ms-finances | `POST /api/v1/finances/transactions` (`FinancesGatewayImpl`) | Records buy/sell cash leg transactions using broker sentinel CBU (`INVEST_BROKER_CBU_*`) |
