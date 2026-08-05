# ms-investments — API

Endpoints and error codes. Envelope shape, exception hierarchy and the DomainError → HTTP
mapping: parent `.ai/references/APP_STRUCTURE.md` — not repeated here.

## Endpoints

| Method | Path | Purpose | Error codes |
|---|---|---|---|
| GET | `/api/v1/investments/holdings` | List user holdings (optional `?assetType=`) | — |
| GET | `/api/v1/investments/holdings/valuation` | Derived investment read-model valuation (`?bankNumber=&currency=`) | `invalid_bank_number`, `invalid_currency` |
| POST | `/api/v1/investments/holdings` | Create holding (records buy transaction in ms-finances if `fundingCbu` set) | `resource_already_exists`, `invalid_ticker`, `finances_service_unavailable` |
| PUT | `/api/v1/investments/holdings/{id}` | Update quantity, average purchase price or thresholds | `resource_not_found`, `invalid_quantity` |
| DELETE | `/api/v1/investments/holdings/{id}` | Close/sell holding (records proceeds in ms-finances if `destinationCbu` set) | `resource_not_found`, `finances_service_unavailable` |
| GET | `/api/v1/investments/portfolio/summary` | Aggregated portfolio valuation, total P&L, allocation breakdown | — |
| GET | `/api/v1/investments/portfolio/holdings` | List holdings enriched with live prices and P&L % | — |
| GET | `/api/v1/investments/portfolio/holdings/{id}` | Single holding detail with live price and P&L % | `resource_not_found` |
| GET | `/api/v1/investments/portfolio/evolution` | Historical portfolio value evolution chart (`?days=`) | `invalid_date_range` |
| POST | `/api/v1/investments/prices/refresh` | Trigger full price refresh for all active tickers | `iol_service_unavailable` |
| GET | `/api/v1/investments/prices/history/{ticker}` | OHLC price history for a ticker (`?from=&to=`) | `resource_not_found`, `invalid_date_range` |
| GET | `/api/v1/investments/market/discovery` | Trending market opportunities not in portfolio (`?limit=`) | `iol_service_unavailable` |
| GET | `/api/v1/investments/market/panel` | Market panel quotes, indices (MERVAL/SP500), and latest FX rates | — |
| GET | `/api/v1/investments/fx/rates` | Historical persisted FX rates (`?from=&to=&view=`) | `invalid_date_range` |
| GET | `/api/v1/investments/fx/rates/latest` | Latest persisted FX rates for each view | — |
| GET | `/api/v1/investments/fx/rates/at` | Computed FX rates at a specific date (`?date=`) | `invalid_date_range` |
| POST | `/api/v1/investments/fx/rates/backfill` | Idempotent backfill of FX rates (`?from=&to=`) | `iol_service_unavailable` |
| PUT | `/api/v1/investments/fees/brokers/{bankNumber}` | Upsert broker fee schedule for a bank | `invalid_fee_schedule` |
| GET | `/api/v1/investments/fees/brokers` | List all broker fee schedules | — |

## DomainError catalog

| Slug | HTTP status | When it is thrown |
|---|---|---|
| `resource_not_found` | 404 | Holding, price, or fee schedule lookup found nothing |
| `resource_already_exists` | 409 | Holding for `(userId, bankNumber, ticker)` already exists |
| `resource_conflict` | 409 | Operation conflicts with current holding state |
| `holding_quantity_non_positive` | 422 | Quantity is zero or negative |
| `holding_currency_mismatch` | 422 | Purchase currency differs from existing asset currency |
| `invalid_ticker` | 400 | Ticker symbol contains invalid characters |
| `invalid_bank_number` | 400 | Bank number is not 3 digits |
| `invalid_fee_schedule` | 400 | Fee percentage outside `[0, 100]` |
| `invalid_date_range` | 400 | `from` date is after `to` date |
| `iol_service_unavailable` | 503 | IOL API authentication or call failed |
| `finances_service_unavailable` | 500 | Feign call to record cash transaction in ms-finances failed |
| `internal_error` | 500 | Unmapped failure |
