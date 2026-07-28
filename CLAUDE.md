# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# E-Commerce Food System

Spring Boot 3.4.5 / Java 21 REST backend (MySQL) with a separate React (Vite) frontend in `food-frontend/`.

## Build & Run (backend)
Run from the repo root. On Windows use `mvnw.cmd`; the `./mvnw` shell script also exists.
- Run: `mvnw.cmd spring-boot:run` (serves on **http://localhost:8081**)
- Test (all): `mvnw.cmd test`
- Single test class: `mvnw.cmd test -Dtest=ECommerceFoodSystemApplicationTests`
- Single method: `mvnw.cmd test -Dtest=ClassName#methodName`
- Package: `mvnw.cmd clean install`

There are no Spring profiles defined — a single `application.properties` is used for dev.

The only test is the `ECommerceFoodSystemApplicationTests` context-load smoke test, so `mvnw.cmd test` mainly proves the app boots and the JPA mappings validate — it is not a behavioural safety net.

## Frontend (`food-frontend/`)
React 19 + Vite + react-router-dom 7, axios for API calls.
- Dev server: `npm run dev` (Vite on **http://localhost:5173**)
- Lint: `npm run lint`  •  Build: `npm run build`
- All requests go through `src/api/axios.js`: `baseURL = import.meta.env.VITE_API_URL ?? 'http://localhost:8081'` — defaults to the local backend, overridden to `''` (relative `/api`) in production. `src/api/services.js` wraps the endpoints, one exported `*Api` object per domain.
- `CorsConfig.java` allows `app.cors.allowed-origins` (env `CORS_ALLOWED_ORIGINS`, comma-separated) on `/api/**`, defaulting to `http://localhost:5173` — keep the Vite port in sync with it. (CORS is irrelevant in the Dockerized/VPS deploys, where Nginx fronts both on one origin.)

## Authentication & client-side state
There is **no session, token, or filter-chain security** — only `spring-security-crypto` is on the classpath (for BCrypt); there is no Spring Security web layer. Every `/api/**` endpoint is open, and access control is enforced only in the React client, so never assume the server authenticates a caller.
- **Customer login** — `POST /api/customers/login` with `{email, password}`; `CustomerServiceImpl` verifies against a BCrypt hash and throws `401` on mismatch. `createCustomer`/`updateCustomer` hash the incoming password (update only re-hashes when a new one is sent), and `toDTO` **never copies the hash out**. The `PasswordEncoder` bean is declared in `config/CorsConfig.java` (there is no separate security config class). `AuthContext.jsx` stores the returned customer in `localStorage` (`foodapp.customer`); its header comment claiming the backend has no password auth is stale.
- **Admin login** posts to `POST /api/admins/login` — still **plaintext comparison** in `AdminServiceImpl`, not BCrypt. `AdminAuthContext.jsx` strips the password and stores the admin in `localStorage` (`foodapp.admin`). The `/admin/*` routes are gated purely client-side by `RequireAdmin.jsx`.
- **The cart is entirely client-side** (`CartContext.jsx`, `localStorage` key `foodapp.cart`) — it stores product snapshots + quantity and is converted into an order only at checkout. The backend `cart`/`cart_item` tables and their controllers exist but are **not** what the UI cart uses.
- Routing (`App.jsx`): two shells — a customer shell (`CustomerLayout` = navbar + container) and an admin shell (`AdminLayout`, no customer navbar) behind `RequireAdmin`.

## Delivery lifecycle
The largest piece of real domain logic, spread across `DeliveryServiceImpl`, the `delivery` + `delivery_decline` tables, and the admin/customer React pages. Read the whole service before touching any one transition — the states are mutually constraining and every transition validates the current state, throwing `409 CONFLICT` or `400 BAD_REQUEST` rather than silently no-oping.

```
Preparing --assign--> Assigned --accept--> Shipped --code--> Delivered --confirm--> Completed
    ^                     |                                       |
    +------ decline ------+                                       +--problem--> Disputed
```

- **Assign** (`POST /{id}/assign`, admin) only *offers* the job: it sets the courier, moves to `Assigned`, stamps `assigned_at`, and mints a single-use `accept_token`. Naming a courier is not the same as starting the delivery.
- **Accept** (`POST /{id}/accept`, driver) is what actually starts it: → `Shipped`, generates the 6-digit `delivery_code`, and clears `accept_token` so the link cannot be reused.
- **Decline** (`POST /{id}/decline`) returns the delivery to the pool (courier fields nulled, back to `Preparing`) and writes a `delivery_decline` row. `GET /{id}/declined-by` feeds the admin picker, and `assign` re-checks it — the same driver is never re-offered the same delivery.
- **Driver access is by token, not login**: `GET /api/deliveries/token/{token}` resolves the offer for a driver opening the emailed/copied link. The frontend route for this is not wired up in `App.jsx` yet.
- **`delivery_code`** is the 6-digit number the customer reads out to the driver; `POST /{id}/complete` requires an exact match to reach `Delivered`. It is minted at every entry into `Shipped` (accept, an update that transitions to `Shipped`, or a create that starts there) — the check in `updateDelivery` **must** stay above the `setDeliveryStatus` call that overwrites the old status.
- **`Delivered` → `Completed`** happens either by the customer confirming (`POST /{id}/confirm`) or automatically: `autoConfirmDeliveries()` is `@Scheduled(fixedRate = 3600000)` and sweeps anything `Delivered` for more than 3 days. Scheduling is enabled by `@EnableScheduling` on `ECommerceFoodSystemApplication`.
- **`updateDelivery` merges, it does not replace**: blank courier/phone/address fields in the admin form are ignored so editing the status dropdown cannot wipe a driver who already accepted. Keep that behaviour when adding fields.
- `Delivery.DeliveryStatus` and the `delivery.delivery_status` DB enum must stay in sync (last changed in `V12`), the same way `Payment.PaymentMethod` must. Note `Orders.OrderStatus` (`Pending, Processing, Shipped, Delivery, Cancelled`) is a **separate, unsynchronised** enum — an order's status is not derived from its delivery's.

## Payments (Bakong KHQR)
Cambodia's Bakong KHQR is the one integration that reaches an external service; everything else is local CRUD.
- Slice: `BakongController` (`/api/payment/bakong`) → `BakongService`/`BakongServiceImpl` → `KhqrGenerator` (builds the EMVCo KHQR string + MD5) and `config/BakongProperties` (`bakong.*` in `application.properties`, all env-overridable). No entity of its own — it writes rows into the existing `Payment` table with `PaymentMethod.Bakong`.
- Flow: `POST /qr/{orderId}` builds a KHQR for the order total and upserts a single **Pending** Bakong `Payment` (retried checkouts reuse the existing Pending row, not stack duplicates). The client then polls `GET /status?md5=&orderId=`, which calls Bakong's `check_transaction_by_md5`; `responseCode == 0` means paid, and the service flips the order's Bakong payment(s) to **Paid**. Frontend side lives in `food-frontend/src/pages/Checkout.jsx`.
- Config guard: `application.properties` ships **placeholders** (`your_name@bank`, `REPLACE_WITH_YOUR_TOKEN`). `requireConfigured()` throws `503 SERVICE_UNAVAILABLE` if they aren't overridden, so QR generation fails fast instead of producing QRs that never settle. Set `BAKONG_ACCOUNT_ID` + `BAKONG_API_TOKEN` (from https://api-bakong.nbc.gov.kh) to enable. A 401/403 from Bakong surfaces as `502` (bad token); network/parse errors are swallowed as "not paid yet" so polling continues.
- `V5__add_bakong_payment_method.sql` adds `Bakong` to the `payment.payment_method` enum — the DB enum and `Payment.PaymentMethod` must stay in sync.

## Database & Migrations
- **MySQL**, schema `e_commerce_system` on `localhost:3306`. Connection comes from env vars with dev defaults baked into `application.properties`: `DB_URL` (default `jdbc:mysql://localhost:3306/e_commerce_system`), `DB_USERNAME` (default `root`), `DB_PASSWORD` (default `root`). Docker/prod override these — never hardcode creds.
- **Flyway owns the schema.** Migrations live in `src/main/resources/db/migration/` (currently `V1__init.sql` … `V12__…`). JPA runs with `ddl-auto=validate`, so Hibernate will **not** create or alter tables — any schema **or seed-data** change must be a new `V{n}__description.sql` migration, or the app fails to start on validation. `V10` exists precisely because entity fields were added without a migration and fresh (Docker/prod) databases then failed validation.
- Note `V12__add delivery_assignment_flow.sql` has a **space** in its filename; quote the path in shell commands.
- **Applied migrations are immutable.** Flyway checksums every applied migration; editing an already-run `V{n}` file makes the app fail to boot with a "checksum mismatch" on the next start. Fix forward with a new migration. If you must realign a dev DB after editing one (last resort, dev only): `mvnw.cmd org.flywaydb:flyway-maven-plugin:10.20.1:repair -Dflyway.url=... -Dflyway.user=root -Dflyway.password=root` (the plugin isn't in `pom.xml`, so invoke it by full coordinates). Note `repair` only updates the stored checksum — it does **not** re-run the migration, so existing rows are unchanged.
- Tables: admin, cart, cart_item, category, customer, delivery, delivery_decline, orders, payment, products, review.
- Tests use an in-memory **H2** DB (`MODE=MySQL`) with Flyway disabled and `ddl-auto=create-drop` (`src/test/resources/application.properties`). Because Flyway is off under test, a migration-only change is never exercised by the test suite — verify it against a real MySQL.

## Docker & Deployment
Two distinct deployment paths (both documented in `deploy/README.md`):
- **Docker Compose (`docker-compose.yml`)** — full stack: `mysql:8` + backend (multi-stage `Dockerfile`, fat JAR) + frontend (`food-frontend/Dockerfile`, Nginx). The app is served on **http://localhost:8080**; Nginx proxies `/api` to the backend, so frontend+API share one origin and **no CORS config applies**. The backend container is *not* published to the host (only reachable by the frontend container). Secrets come from a `.env` file (copy `.env.example`): `MYSQL_ROOT_PASSWORD`, `DB_USERNAME`, `DB_PASSWORD`. Run: `docker compose up -d --build`.
- **Single Ubuntu VPS (`deploy/`)** — MySQL + the JAR under systemd (`foodapp.service`) + Nginx (`nginx-foodapp.conf`) serving the built frontend and proxying `/api`. `deploy/deploy.sh` builds JAR+frontend locally, uploads, and restarts. Same "one origin, no CORS" model.
- In both, the React app makes **relative `/api` calls** in production (`food-frontend/.env.production` sets `VITE_API_URL=` empty); the hardcoded `localhost:8081` baseURL only applies to local `npm run dev`.

## Architecture
Java package root: `com.example.E_commerce_food_system` (note the capitalized layer directories). Each domain follows the same vertical slice:

`Controller/` → `Service/` (interface + `*ServiceImpl` live in the same `Service/` package, e.g. `ProductService.java` + `ProductServiceImpl.java`) → `Repository/` (Spring Data JPA) → `Entity/`, with `DTO/` crossing the controller boundary.

Conventions enforced across all features:
- **Never expose entities from controllers** — controllers accept and return `DTO/` types only.
- **DTO ⇄ Entity mapping is manual**, done by private `toDTO`/`toEntity` helpers inside each `*ServiceImpl` (no MapStruct/ModelMapper). When adding a field, update both helpers.
- **Service interface + `ServiceImpl` pair** for every domain; controllers depend on the interface.
- **Constructor injection only** (no `@Autowired` fields). `DeliveryServiceImpl` currently violates this with three `@Autowired` fields — follow the convention in new code rather than copying it.
- **Errors use `org.springframework.web.server.ResponseStatusException`** (e.g. `HttpStatus.NOT_FOUND`) thrown from the service layer — there is no custom exception package or `@ControllerAdvice`. `server.error.include-message=always` is set so the `reason` reaches the client. A few older paths still throw bare `RuntimeException` (→ opaque 500s); convert them when you touch them.
- Endpoints that take a single scalar (a delivery code, an assignment) accept a `Map<String, String>` body rather than a dedicated request DTO — see `DeliveryController`.
- Entity IDs are `Integer`; path variables and service methods use `Integer`.
- Lombok is on the classpath; mapping is still written out by hand in services.

## Layout
- `src/main/java/com/example/E_commerce_food_system/`
  - `config/` — `CorsConfig` (also holds the `PasswordEncoder` bean), `BakongProperties`
  - `Controller/` `Service/` (interfaces + `*ServiceImpl` together) `Repository/` `Entity/` `DTO/`
  - `ECommerceFoodSystemApplication.java` — entry point, `@EnableScheduling`
- `src/main/resources/` — `application.properties`, `db/migration/`
- `food-frontend/` — React app (`src/pages`, `src/components`, `src/admin`, `src/context`, `src/api`)
