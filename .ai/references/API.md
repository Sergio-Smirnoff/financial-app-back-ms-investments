# ms-investments — API

Endpoints and error codes. Envelope shape, exception hierarchy and the DomainError → HTTP
mapping: parent `.ai/references/APP_STRUCTURE.md` — not repeated here.

## Endpoints

| Method | Path | Purpose | Error codes |
|---|---|---|---|
| GET | `/api/v1/investments/holdings` | List user holdings (optional `?assetType=`) | — |
| GET | `/api/v1/investments/holdings/valuation` | Derived investment read-model valuation (`?bankNumber=&currency=`) | `invalid_bank_number`, `invalid_currency` |
| POST | `/api/v1/investments/holdings` | Create holding (records buy transaction in ms-finances if `fundingCbu` set) | `resource_already_exists`, `invalid_ticker`, `finances_service_unavailable` |
| PUT | `/api/v1/investments/holdings/{id}` | Update ticker, asset type, quantity, average purchase price or thresholds | `resource_not_found`, `invalid_quantity` |
| DELETE | `/api/v1/investments/holdings/{id}` | Close/sell every unit at the market price (same use case as `POST …/sell`; records proceeds in ms-finances if `destinationCbu` set) | `resource_not_found`, `finances_service_unavailable` |
| POST | `/api/v1/investments/holdings/{id}/sell` | Sell part or all of a holding. Body `{quantity, price?, destinationCbu?}`: `price` absent → stored market quote (cost fallback), present → that quote in the holding's currency (bonds: per 100 VN, like the market quote). Books `quantity × price`, net of the broker fee schedule, to `destinationCbu` in ms-finances; a partial sale keeps the average cost; selling every unit deletes the holding. → `{holdingId, soldQuantity, remainingQuantity, proceeds, bookedAmount, currency, closed}` | `validation_error`, `resource_not_found`, `holding_sale_exceeds_quantity`, `finances_service_unavailable` |
| GET | `/api/v1/investments/portfolio/summary` | Aggregated portfolio valuation, total P&L, allocation breakdown | — |
| GET | `/api/v1/investments/portfolio/holdings` | List holdings enriched with live prices and P&L % | — |
| GET | `/api/v1/investments/portfolio/holdings/{id}` | Single holding detail with live price and P&L % | `resource_not_found` |
| GET | `/api/v1/investments/portfolio/evolution` | Historical portfolio value evolution chart (`?days=`) | `invalid_date_range` |
| POST | `/api/v1/investments/portfolio/snapshot/ensure-today` | Capture today's (ART) snapshot for the caller unless it exists → `{created, date}`; idempotent, safe to call on every start-up | — |
| GET | `/api/v1/investments/positions/search` | Search user positions by ticker or name (`?q=`) | — |
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

## Valuation notes

- Position values and sale proceeds follow the per-100 rule for `BOND` (see `DOMAIN.md` § Valuation rule); every other type is per unit.
- `positions/search` `marketValue` is the position's cost basis (`Holding.costBasis`), not a live value.
- `HoldingWithPriceResult.currentPrice` (holdings and portfolio-holdings `currentPrice`): for a bond it is the raw per-100 quote when a price exists, but the per-1 average cost when no price exists (pre-existing). Clients must not derive values from it; use the returned `marketValue` and P&L fields.
- `portfolio/summary` `byCurrency[].breakdown[]` entry: `assetType`, `totalValue`, `totalCost`, `totalPl`, `percentage` (decimal strings, in the bucket's currency; `totalPl = totalValue − totalCost`, `percentage` = share of the bucket's `totalValue`) and `count` (integer, holdings of that type in the bucket). ms-gateway reads `totalCost` and `count` strictly — deploy this service before a gateway that reads them.

## DomainError catalog

| Slug | HTTP status | When it is thrown |
|---|---|---|
| `resource_not_found` | 404 | Holding, price, or fee schedule lookup found nothing |
| `resource_already_exists` | 409 | Holding for `(userId, bankNumber, ticker)` already exists |
| `resource_conflict` | 409 | Operation conflicts with current holding state |
| `holding_quantity_non_positive` | 422 | Quantity is zero or negative |
| `holding_currency_mismatch` | 422 | Purchase currency differs from existing asset currency |
| `holding_sale_exceeds_quantity` | 422 | A sell asks for more units than the holding has |
| `invalid_ticker` | 400 | Ticker symbol contains invalid characters |
| `invalid_bank_number` | 400 | Bank number is not 3 digits |
| `invalid_fee_schedule` | 400 | Fee percentage outside `[0, 100]` |
| `invalid_date_range` | 400 | `from` date is after `to` date |
| `iol_service_unavailable` | 503 | IOL API authentication or call failed |
| `finances_service_unavailable` | 500 | Feign call to record cash transaction in ms-finances failed |
| `internal_error` | 500 | Unmapped failure |
