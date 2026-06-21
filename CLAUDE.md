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

## Frontend (`food-frontend/`)
React 19 + Vite + react-router-dom 7, axios for API calls.
- Dev server: `npm run dev` (Vite on **http://localhost:5173**)
- Lint: `npm run lint`  •  Build: `npm run build`
- All requests go through `src/api/axios.js`: `baseURL = import.meta.env.VITE_API_URL ?? 'http://localhost:8081'` — defaults to the local backend, overridden to `''` (relative `/api`) in production. `src/api/services.js` wraps the endpoints.
- `CorsConfig.java` only allows origin `http://localhost:5173` on `/api/**` — keep the Vite port in sync with it. (CORS is irrelevant in the Dockerized/VPS deploys, where Nginx fronts both on one origin.)

## Authentication & client-side state
There is **no real auth, security, or session layer** — no Spring Security, tokens, cookies, or password hashing. Every `/api/**` endpoint is open; access control is enforced only in the React client, so never assume the server authenticates a caller.
- **Customer "login"** (`AuthContext.jsx`) is just `GET /api/customers/email/{email}`; the returned customer is kept in `localStorage` (`foodapp.customer`). Register = create the customer, then store it. No password is checked on the customer side.
- **Admin login** (`AdminAuthContext.jsx`) posts to `POST /api/admins/login` (email + password compared plaintext in `AdminServiceImpl`), strips the password, and stores the admin in `localStorage` (`foodapp.admin`). The `/admin/*` routes are gated purely client-side by `RequireAdmin.jsx`.
- **The cart is entirely client-side** (`CartContext.jsx`, `localStorage` key `foodapp.cart`) — it stores product snapshots + quantity and is converted into an order only at checkout. The backend `cart`/`cart_item` tables and their controllers exist but are **not** what the UI cart uses.
- Routing (`App.jsx`): two shells — a customer shell (`CustomerLayout` = navbar + container) and an admin shell (`AdminLayout`, no customer navbar) behind `RequireAdmin`.

## Database & Migrations
- **MySQL**, schema `e_commerce_system` on `localhost:3306`. Connection comes from env vars with dev defaults baked into `application.properties`: `DB_URL` (default `jdbc:mysql://localhost:3306/e_commerce_system`), `DB_USERNAME` (default `root`), `DB_PASSWORD` (default `root`). Docker/prod override these — never hardcode creds.
- **Flyway owns the schema.** Migrations live in `src/main/resources/db/migration/` (`V1__init.sql` … `V5__...`). JPA runs with `ddl-auto=validate`, so Hibernate will **not** create or alter tables — any schema **or seed-data** change must be a new `V{n}__description.sql` migration, or the app fails to start on validation.
- **Applied migrations are immutable.** Flyway checksums every applied migration; editing an already-run `V{n}` file makes the app fail to boot with a "checksum mismatch" on the next start. Fix forward with a new migration. If you must realign a dev DB after editing one (last resort, dev only): `mvnw.cmd org.flywaydb:flyway-maven-plugin:10.20.1:repair -Dflyway.url=... -Dflyway.user=root -Dflyway.password=root` (the plugin isn't in `pom.xml`, so invoke it by full coordinates). Note `repair` only updates the stored checksum — it does **not** re-run the migration, so existing rows are unchanged.
- Tables: admin, cart, cart_item, category, customer, delivery, orders, payment, products, review.
- Tests use an in-memory **H2** DB (`MODE=MySQL`) with Flyway disabled and `ddl-auto=create-drop` (`src/test/resources/application.properties`).

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
- **Constructor injection only** (no `@Autowired` fields).
- **Errors use `org.springframework.web.server.ResponseStatusException`** (e.g. `HttpStatus.NOT_FOUND`) thrown from the service layer — there is no custom exception package or `@ControllerAdvice`.
- Entity IDs are `Integer`; path variables and service methods use `Integer`.
- Lombok is on the classpath; mapping is still written out by hand in services.

## Layout
- `src/main/java/com/example/E_commerce_food_system/`
  - `config/` — e.g. `CorsConfig`
  - `Controller/` `Service/` (interfaces + `*ServiceImpl` together) `Repository/` `Entity/` `DTO/`
  - `ECommerceFoodSystemApplication.java` — entry point
- `src/main/resources/` — `application.properties`, `db/migration/`
- `food-frontend/` — React app (`src/pages`, `src/components`, `src/admin`, `src/context`, `src/api`)
