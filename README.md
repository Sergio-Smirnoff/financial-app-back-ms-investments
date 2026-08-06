# ms-investments

Holdings CRUD, portfolio P&L, live IOL price feed, price history, and notification thresholds.

**Port:** 8086 | **Schema:** `investments`

---

## Tech Stack

- Java 21, Spring Boot 3.4.2 (MVC), Spring Data JPA, Flyway
- IOL API client — OAuth2 password-grant, Resilience4j `@Retry` + `@CircuitBreaker`. Per-instrument currency is read from IOL's `moneda` field (`IolCurrencyResolver`: `dolar*` → USD, else ARS); never hardcoded.
- Kafka producer (CloudEvents 1.0, binary mode): `investments.threshold.breached` via transactional outbox + commons `OutboxRelay`; producer-only (no consumers)
- MapStruct, Lombok
- Feign client → ms-finances (buy/sell cash-flow recording)

---

## Domain

**Holding** — ticker, name, AssetType (STOCK / BOND / CEDEAR / FCI), HoldingQuantity, Money avgPurchasePrice, currency, `BankNumber` (holdings are keyed by bank + currency; the derived "investment account" is a read-model Σ price×qty, not a real account), ThresholdConfig (`gainPct`, `lossPct`), NotificationTimestamps (`lastGainNotifiedAt`, `lastLossNotifiedAt`).

**AssetPrice** — live OHLC snapshot per ticker (lastPrice, openPrice, highPrice, lowPrice, volume, dailyVariation, pricedAt).

**AssetPriceHistory** — immutable OHLC snapshot inserted on every price refresh; used for charting.

**MarketQuote** — panel quote for market discovery (symbol, lastPrice, dailyVariation).

**MarketIndex** — market index points (code, value, variation, updatedAt) for MERVAL, SP500, RIESGO_PAIS.

**FxRate** — exchange rate aggregate (date, view [MEP/CCL/OFICIAL], buy, sell, source [IOL_SYNTHETIC/IOL_DIRECT/MANUAL]).

**BrokerFeeSchedule** — fee schedule per broker bank & asset type (buyFeePct, sellFeePct, minimumFee, marketFeePct, ivaTreatment).

**PortfolioSnapshot** — daily EOD per-user totals (JSONB by currency) for the evolution chart.

**RefreshJob** — tracks in-flight / completed price refresh jobs.

### Scheduled jobs

| Scheduler | Trigger | Purpose |
|---|---|---|
| `PriceRefreshScheduler` | `IOL_PRICE_REFRESH_CRON` (weekdays 10–17 ARS) | Refresh live prices + evaluate thresholds |
| `FxRateSyncScheduler` | `IOL_PRICE_REFRESH_CRON` (weekdays 10–17 ARS) | Sync synthetic MEP/CCL and official FX rates |
| `MarketDiscoveryScheduler` | Fixed-rate `IOL_DISCOVERY_REFRESH_RATE` (default 15 min) | Sync panel quotes |
| `PortfolioSnapshotScheduler` | Daily midnight | Capture per-user portfolio snapshots |

After each price refresh `EvaluateThresholdsUseCase` checks P&L % against each holding's `ThresholdConfig`. On breach it writes an `investments.threshold.breached` CloudEvent (1.0, binary mode; `data` = `InvestmentThresholdData`) to the `outbox_event` table in the same DB transaction — the commons `OutboxRelay` publishes it to Kafka (consumed by ms-notifications) — and stamps the matching `NotificationTimestamps` field to prevent re-notification.

### IOL historical series — null/zero price filtering

`IolGatewayImpl.getHistoricalSeries` now drops any historical point whose `lastPrice` is null or ≤ 0 (no-trade sessions and pre-open candles returned by IOL before the market opens). This eliminates the trailing drop-to-zero spike that appeared on price charts when a refresh ran before the first trade of the day.

### Bank-contract integration

There is no INVESTMENT account in ms-banks — it was removed. A holding belongs to a `BankNumber`, and the "investment account" shown in the UI is a derived read-model (Σ price×qty grouped by bank + currency). Cash for buys and sells flows as CBU-to-CBU transactions recorded in ms-finances. `FinancesGatewayImpl` resolves the broker sentinel CBU from env vars (`INVEST_BROKER_CBU_ARS`, `INVEST_BROKER_CBU_USD`, …) by currency code. A `FinancesServiceException` aborts the entire operation before the holding is persisted.

---

## API Endpoints

All endpoints return the shared envelope `{ status, title, code, message, data }` from
`commons-core` (built from `financial-app-parent`). `code` appears only on errors and carries the
`DomainError` slug (e.g. `holding_quantity_invalid`); error details travel in `data`. Errors are
rendered by `GlobalExceptionHandler extends ApiExceptionHandler` (commons-web), and endpoints
declare their throwable codes with `@ApiErrorCodes` (generated Swagger examples).

### Holdings — `/api/v1/investments/holdings`

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/investments/holdings` | List user holdings, paginated; optional `?assetType=` filter |
| `GET` | `/api/v1/investments/holdings/valuation?accountCbu=` | Total valuation for holdings linked to a CBU |
| `GET` | `/api/v1/investments/holdings/count?accountCbu=` | Count of holdings linked to a CBU |
| `POST` | `/api/v1/investments/holdings` | Create holding (records buy transaction in ms-finances if `fundingCbu` provided) |
| `PUT` | `/api/v1/investments/holdings/{id}` | Update holding fields |
| `DELETE` | `/api/v1/investments/holdings/{id}?destinationCbu=` | Close (sell) holding; records sale proceeds in ms-finances if `destinationCbu` provided |

### Portfolio — `/api/v1/investments/portfolio`

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/investments/portfolio/summary` | Portfolio totals, P&L, allocation breakdown per user |
| `GET` | `/api/v1/investments/portfolio/holdings` | All holdings enriched with current price and P&L |
| `GET` | `/api/v1/investments/portfolio/holdings/{holdingId}` | Single holding detail with current price and P&L |
| `GET` | `/api/v1/investments/portfolio/evolution?days=` | Historical portfolio evolution (ARS + USD) for N days |

### Positions — `/api/v1/investments/positions`

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/investments/positions/search?q=` | Search user positions by ticker or name |

### Prices — `/api/v1/investments/prices`

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/investments/prices/refresh` | Manually trigger a full price refresh for all held tickers |

### Price History — `/api/v1/investments/prices/history`

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/investments/prices/history/{ticker}?from=&to=` | OHLC price history for a ticker (ISO datetime; omit both for all history) |

### Market Discovery — `/api/v1/investments/market`

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/investments/market/discovery?limit=` | Trending assets not in the user's portfolio (default limit 5) |
| `GET` | `/api/v1/investments/market/panel` | Widened market panel containing quotes, indices, and latest FX rates |

### FX Rates — `/api/v1/investments/fx/rates`

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/investments/fx/rates?from=&to=&view=` | Historical persisted FX rates |
| `GET` | `/api/v1/investments/fx/rates/latest` | Latest persisted FX rate for each view |
| `GET` | `/api/v1/investments/fx/rates/at?date=` | Computed FX rates passthrough at a specific date |
| `POST` | `/api/v1/investments/fx/rates/backfill?from=&to=` | Idempotent manual backfill of FX rates |

### Broker Fees — `/api/v1/investments/fees/brokers`

| Method | Path | Purpose |
|---|---|---|
| `PUT` | `/api/v1/investments/fees/brokers/{bankNumber}` | Upsert fee schedule for a broker/bank |
| `GET` | `/api/v1/investments/fees/brokers` | List all broker fee schedules |

All endpoints return `ApiResponse<T>` (`success`, `message`, `data`, `errors`, `timestamp`). Numeric fields serialised as `String` to avoid JSON precision loss.

---

## Source Tree

```
src/main/java/com/financialapp/investments/
├── InvestmentsApplication.java
├── domain/
│   ├── common/
│   │   ├── DomainEvent.java
│   │   └── model/
│   │       ├── Cbu.java
│   │       ├── Money.java
│   │       ├── PageRequest.java
│   │       ├── PageResult.java
│   │       └── UserId.java
│   ├── event/
│   │   ├── Direction.java
│   │   ├── HoldingClosedEvent.java
│   │   ├── HoldingCreatedEvent.java
│   │   ├── HoldingUpdatedEvent.java
│   │   └── PriceThresholdBreachedEvent.java
│   ├── exception/
│   │   ├── DomainError.java
│   │   ├── DomainException.java
│   │   ├── FinancesServiceException.java
│   │   ├── IolServiceException.java
│   │   ├── ResourceAlreadyExistsException.java
│   │   ├── ResourceConflictException.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── UnsupportedCurrencyException.java
│   │   └── holding/
│   │       ├── HoldingCurrencyMismatchException.java
│   │       └── HoldingQuantityNonPositiveException.java
│   ├── gateway/
│   │   ├── DomainEventPublisher.java
│   │   ├── FinancesGateway.java
│   │   ├── HoldingQueryGateway.java
│   │   ├── IolGateway.java
│   │   └── SupportedCurrencies.java
│   ├── model/
│   │   ├── history/
│   │   │   ├── AssetPriceHistory.java
│   │   │   ├── AssetPriceHistoryId.java
│   │   │   └── HistoricalPricePoint.java
│   │   ├── holding/
│   │   │   ├── Holding.java
│   │   │   ├── HoldingFilter.java
│   │   │   ├── HoldingId.java
│   │   │   ├── HoldingQuantity.java
│   │   │   ├── NotificationTimestamps.java
│   │   │   ├── ThresholdConfig.java
│   │   │   └── Ticker.java
│   │   ├── market/
│   │   │   └── MarketQuote.java
│   │   ├── price/
│   │   │   ├── AssetPrice.java
│   │   │   ├── AssetPriceId.java
│   │   │   ├── AssetType.java
│   │   │   └── PriceDetail.java
│   │   ├── refresh/
│   │   │   ├── RefreshJob.java
│   │   │   ├── RefreshJobId.java
│   │   │   └── RefreshJobStatus.java
│   │   └── snapshot/
│   │       ├── PortfolioSnapshot.java
│   │       └── PortfolioSnapshotId.java
│   ├── repository/
│   │   ├── AssetPriceHistoryRepository.java
│   │   ├── AssetPriceRepository.java
│   │   ├── HoldingRepository.java
│   │   ├── MarketQuoteRepository.java
│   │   ├── PortfolioSnapshotRepository.java
│   │   └── RefreshJobRepository.java
│   └── usecase/
│       ├── holding/
│       │   ├── CloseHoldingUseCase.java
│       │   ├── CreateHoldingUseCase.java
│       │   ├── GetAccountValuationUseCase.java
│       │   ├── GetHoldingDetailUseCase.java
│       │   ├── ListHoldingsUseCase.java
│       │   ├── UpdateHoldingUseCase.java
│       │   ├── command/
│       │   └── response/
│       ├── market/
│       │   ├── GetMarketDiscoveryUseCase.java
│       │   ├── SyncMarketQuotesUseCase.java
│       │   ├── command/
│       │   └── response/
│       ├── portfolio/
│       │   ├── GetHoldingsWithPricesUseCase.java
│       │   ├── GetPortfolioEvolutionUseCase.java
│       │   ├── GetPortfolioSummaryUseCase.java
│       │   ├── command/
│       │   └── response/
│       ├── price/
│       │   ├── EvaluateThresholdsUseCase.java
│       │   ├── GetPriceHistoryUseCase.java
│       │   ├── RefreshPricesUseCase.java
│       │   └── command/
│       └── snapshot/
│           └── CapturePortfolioSnapshotUseCase.java
├── application/
│   ├── holding/impl/
│   ├── market/impl/
│   ├── portfolio/impl/
│   ├── price/impl/
│   └── snapshot/impl/
├── infrastructure/
│   ├── config/
│   │   ├── CacheConfig.java
│   │   ├── CurrenciesProperties.java
│   │   ├── FeignConfig.java
│   │   ├── InternalAuthFilter.java
│   │   ├── InvestmentsIntegrationProperties.java
│   │   ├── IolProperties.java
│   │   ├── KafkaConfig.java
│   │   └── SupportedCurrenciesImpl.java
│   ├── gateway/
│   │   ├── IolApiClient.java
│   │   ├── FinancesClient.java
│   │   ├── dto/
│   │   └── impl/
│   ├── messaging/
│   │   ├── KafkaDomainEventPublisher.java       # DomainEventPublisher → writes outbox records
│   │   ├── mapper/   (InvestmentThresholdEventMapper)
│   │   └── payload/  (InvestmentThresholdData)
│   ├── persistence/
│   │   ├── entity/
│   │   ├── jpa/
│   │   ├── mapper/
│   │   └── repository/
│   └── scheduler/
│       ├── MarketDiscoveryScheduler.java
│       ├── PortfolioSnapshotScheduler.java
│       └── PriceRefreshScheduler.java
└── web/
    ├── controller/
    │   ├── HoldingController.java
    │   ├── MarketDiscoveryController.java
    │   ├── PortfolioController.java
    │   ├── PriceController.java
    │   └── PriceHistoryController.java
    ├── dto/
    │   ├── request/
    │   └── response/
    ├── error/
    │   └── GlobalExceptionHandler.java
    └── mapper/
        ├── HoldingWebMapper.java
        ├── MarketWebMapper.java
        ├── PortfolioWebMapper.java
        └── PriceWebMapper.java
```

---

## Key Env Vars

| Variable | Purpose |
|---|---|
| `IOL_BASE_URL` | IOL API base URL |
| `IOL_USERNAME` / `IOL_PASSWORD` | IOL OAuth2 credentials |
| `IOL_PRICE_REFRESH_CRON` | Cron for `PriceRefreshScheduler` |
| `IOL_DISCOVERY_REFRESH_RATE` | Fixed-rate ms for `MarketDiscoveryScheduler` (default 900000) |
| `INVEST_BROKER_CBU_ARS` | Sentinel CBU for ARS buy/sell counter-party |
| `INVEST_BROKER_CBU_USD` | Sentinel CBU for USD buy/sell counter-party |
| `INVEST_FINANCES_CATEGORY_ID` | Fixed id of the "Investments" system category in ms-finances |
| `SPRING_DATASOURCE_URL` | PostgreSQL connection (schema `investments`) |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers (`localhost:9093` for local dev) |

---

## Running Locally

```bash
# From parent workspace root — infra + this service only
./scripts/dev.sh local service-investments

# Or directly from this directory
mvn spring-boot:run

# Run tests
mvn test
```

Swagger UI: http://localhost:8086/swagger-ui.html

---

> Full design: `docs/specs/services/ms-investments.md` (parent workspace).

## CI/CD

| Workflow | Trigger | Does |
|---|---|---|
| `ci.yml` | PRs; push to develop/master | tests + docker build via shared `backend-ci.yml` |
| `docker-publish.yml` | push to master; `v*` tags | GHCR publish: `latest`, `sha-*`, semver on tags |
| `release.yml` | manual (bump dropdown) | next `vX.Y.Z` tag + Release + versioned publish |

Reusable workflows live in the root repo `Sergio-Smirnoff/financial-app`.
