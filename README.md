# ms-investments

Investments microservice — holdings CRUD, IOL OHLC price feed, portfolio P&L, price history, and threshold-based notifications.

- **Port:** 8086
- **DB schema:** `investments`
- **Tech stack:** Java 21, Spring Boot 3.4.2, Spring MVC, JPA/Hibernate, Flyway, Kafka, Resilience4j, MapStruct, Lombok

> Full design: `docs/specs/services/ms-investments.md` (parent workspace).

## Run

```bash
# Via dev script (infra + service, hot-reload)
./scripts/dev.sh local service-investments

# Direct Maven
cd back/ms-investments
mvn spring-boot:run

# Tests
mvn test
```

Swagger UI: http://localhost:8086/swagger-ui.html

## Build

```bash
mvn clean package -DskipTests
```

## CI/CD

| Workflow | Trigger | Does |
|---|---|---|
| `ci.yml` | PRs; push to develop/master | tests + docker build via shared `backend-ci.yml` |
| `docker-publish.yml` | push to master; `v*` tags | GHCR publish: `latest`, `sha-*`, semver on tags |
| `release.yml` | manual (bump dropdown) | next `vX.Y.Z` tag + Release + versioned publish |

Reusable workflows live in the root repo `Sergio-Smirnoff/financial-app`.
