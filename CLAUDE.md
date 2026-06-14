# E-Commerce Food System

Spring Boot backend with PostgreSQL database.

## Project Structure
- `backend/src/main/java/com/foodsystem/`
    - `config/` — Spring configuration
    - `domain/` — Entities
    - `repository/` — JPA repositories (Payment, Product, Review, etc.)
    - `service/` — Business logic (Cart, Order, Payment, Product, Review, etc.)
    - `controller/` — REST endpoints
    - `exception/` — Custom exceptions

## Database
- Tables: admin, cart, cart_item, category, customer, delivery, orders, payment, products, review
- Local: `e_commerce_system@localhost`

## Build & Run
- Dev: `./mvnw spring-boot:run -Dspring.profiles.active=dev`
- Test: `./mvnw test`
- Build: `./mvnw clean install`

## Rules
- Always use DTOs — never expose entities directly in controllers
- Never edit `application-prod.yml` directly
- Follow existing Service → ServiceImpl pattern for all new features
- Use constructor injection, not field injection