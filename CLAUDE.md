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
- All requests go through `src/api/axios.js`, hardcoded to `baseURL: http://localhost:8081`. `src/api/services.js` wraps the endpoints.
- `CorsConfig.java` only allows origin `http://localhost:5173` on `/api/**` — keep the Vite port in sync with it.

## Database & Migrations
- **MySQL**, schema `e_commerce_system` on `localhost:3306` (dev creds `root`/`root` in `application.properties`).
- **Flyway owns the schema.** Migrations live in `src/main/resources/db/migration/` (`V1__init.sql`, `V2__...`, …). JPA runs with `ddl-auto=validate`, so Hibernate will **not** create or alter tables — any schema change must be a new `V{n}__description.sql` migration, or the app fails to start on validation.
- Tables: admin, cart, cart_item, category, customer, delivery, orders, payment, products, review.
- Tests use an in-memory **H2** DB (`MODE=MySQL`) with Flyway disabled and `ddl-auto=create-drop` (`src/test/resources/application.properties`).

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
