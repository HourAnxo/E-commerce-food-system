E-Commerce Food Delivery System
A full-stack food ordering and delivery web application built with
React.js, Java Spring Boot, and MySQL. The system is
designed to let customers browse food products, manage a shopping cart,
place orders, and make payments, while providing backend functionality
for managing customers, products, orders, and delivery information.
Academic / learning project. Update this README as features and
deployment configuration evolve. Do not commit real API tokens,
passwords, or other secrets.

1. Purpose of the Application
   The purpose of this application is to provide a convenient online
   platform for ordering food. Customers can view available products, add
   items to their cart, submit orders, and choose a payment method. The
   backend processes application requests and stores the relevant
   information in MySQL.

The project also demonstrates how to build and connect a modern
frontend, a REST API backend, a relational database, and external
payment integration.
2. Intended Users
- Customers: browse food products, manage their cart, place
  orders, provide delivery details, and review order/payment
  information.
- Administrators / staff: manage product and category information,
  review customer and order records, and coordinate delivery
  operations, subject to the admin features enabled in the
  application.
- Developers / students: use the project to learn full-stack
  development, REST API design, database relationships, payment
  integration, and containerization.
3. Main Functions
   Module                              Purpose
   Customer management                 Store customer profile and
   contact/address information

Product catalogue                   Organize products by category and
display product details, prices,
stock, and images

Shopping cart                       Add products to a cart and manage
item quantities

Order management                    Create orders, calculate totals,
and track order status

Delivery management                 Store delivery contact/address
details and delivery progress

Payment management                  Record payment method, payment
status, and payment date

Bakong KHQR                         Generate KHQR payment information
and verify payment status through
the Bakong Open API

Reviews                             Store customer ratings and comments
for products
Administration                      Support operational management of
products, categories, customers,
orders, and related records where
implemented
Typical customer workflow
1. The customer opens the web application.
2. The customer browses products and categories.
3. The customer adds products to the shopping cart.
4. The customer reviews the cart and proceeds to checkout.
5. The customer provides or confirms delivery information.
6. The customer selects a payment method.
7. For Bakong, the backend creates a KHQR payment request and the
   customer scans the QR code with a supported banking app.
8. The backend verifies the transaction with Bakong before treating the
   payment as paid.
9. The order and delivery statuses are updated as the order is
   processed.
   Payment safety: displaying a QR code is not proof of payment. The
   backend must verify the transaction with Bakong before changing a
   payment record to Paid. Never trust a payment-success message sent
   only by the frontend.
4. Technology Stack
   Technology                          Use
   Java 21                             Main backend programming language
   (based on the project runtime log)

Spring Boot 3.4.5                   Backend application framework
(based on the project runtime log)

Spring Web / REST                   Expose HTTP endpoints for the
frontend

Spring Data JPA / Hibernate         Map Java entities to database
tables and perform persistence
operations

Maven                               Manage backend dependencies and
build the Java application

React.js                            Build the interactive frontend

JavaScript / JSX                    Frontend application logic and
components

Tailwind CSS                        Style responsive user interfaces

Vite                                Frontend development server and
build tooling

MySQL 8                             Relational database

Flyway                              Version and apply database schema
migrations (present in project
logs)

Docker                              Package and run application
services in containers

Bakong Open API / KHQR              Support QR-based payments and
transaction verification
IntelliJ IDEA / DataGrip / Postman  Development, database management,
and API testing tools
Versions can change. Check the current pom.xml, frontend
package.json, and Docker configuration before publishing a release.
5. Application Architecture
   The application follows a frontend--backend--database architecture.
   CUSTOMER / ADMIN
   |
   v
   React.js Frontend
   (Vite + Tailwind CSS)
   |
   HTTP / REST API
   |
   v
   Java Spring Boot API
   |
   +-------------+-------------+
   |             |             |
   v             v             v
   Controller     Service      Repository
   REST routes    Business     Data access
   rules            |
   v
   Spring Data JPA
   |
   v
   MySQL DB

              Payment Service / Integration
                        |
                        v
                 Bakong Open API
Backend request flow
1. Controller: receives an HTTP request, validates request input,
   and returns an HTTP response.
2. Service: applies business rules, such as calculating an order
   total or checking payment state.
3. Repository: reads and writes application data using Spring Data
   JPA.
4. Entity: represents a database table as a Java class.
5. DTO: defines the data accepted from or returned to the frontend
   without exposing internal entity details.
6. Database: MySQL stores persistent application data.
   The frontend communicates with the backend through REST endpoints. The
   frontend should not connect directly to MySQL or hold the Bakong secret
   token.
6. Database Design
   The database used in the project is e_commerce_system.

The database design discussed for the project includes these tables:
Table                               Stores
admin                             Administrator account information

customer                          Customer name, email, phone,
address, and creation date

category                          Product category names

products                          Product details, category, price,
stock, and image URL

cart                              A customer's shopping cart

cart_item                         Products and quantities in each
cart

orders                            Customer orders, totals, dates, and
order status

delivery                          Delivery contact, address,
estimated time, and delivery status

payment                           Payment method, payment status, and
payment date
review                            Product ratings and customer
comments
Relationships include customers owning carts and orders, carts
containing cart items, products belonging to categories, and orders
being associated with payment and delivery records.

Use the actual migration scripts or database schema as the source of
truth if the current implementation has changed.
7. Suggested Project Structure
   The exact source tree should be checked against the repository. The
   following is a recommended structure consistent with the Spring Boot
   application and React frontend:
   E-commerce-food-system/
   |
   +-- backend/
   |   +-- pom.xml
   |   +-- src/
   |       +-- main/
   |       |   +-- java/
   |       |   |   +-- com/example/E_commerce_food_system/
   |       |   |       +-- config/
   |       |   |       +-- controller/
   |       |   |       +-- service/
   |       |   |       +-- repository/
   |       |   |       +-- entity/
   |       |   |       +-- dto/
   |       |   |       +-- exception/
   |       |   |       +-- payment/
   |       |   |       +-- ECommerceFoodSystemApplication.java
   |       |   +-- resources/
   |       |       +-- application.properties
   |       |       +-- db/migration/
   |       +-- test/
   |
   +-- frontend/
   |   +-- package.json
   |   +-- index.html
   |   +-- src/
   |       +-- assets/
   |       +-- components/
   |       +-- pages/
   |       +-- services/       # API calls to the backend
   |       +-- hooks/
   |       +-- context/
   |       +-- App.jsx
   |       +-- main.jsx
   |
   +-- Dockerfile              # if using a root-level Dockerfile
   +-- docker-compose.yml
   +-- .gitignore
   +-- README.md
   If your repository keeps the frontend and backend in folders with
   different names, retain those real names rather than renaming folders
   just to match this example.
8. Running the Project Locally
   Requirements
- JDK 21
- Maven (or the Maven wrapper if included)
- Node.js and npm
- MySQL 8
- IntelliJ IDEA or another Java IDE
- A Bakong account and Open API credentials for real Bakong payment
  functionality
  Backend
1. Create or select the MySQL database named e_commerce_system.
2. Configure the database connection in the backend's
   application.properties or environment variables.
3. Configure the Bakong credentials as environment variables. Do not
   hard-code them in source code.
4. Open the backend folder containing pom.xml.
5. Build and run the Spring Boot application:
   mvn clean install
   mvn spring-boot:run
   The historical application log showed the backend starting on port
8081. Check the current configuration for the actual port.
      Frontend
1. Open a terminal in the frontend folder (the folder containing
   package.json).
2. Install dependencies.
3. Start the Vite development server:
   npm install
   npm run dev
   Vite commonly displays the local frontend URL in the terminal, often
   http://localhost:5173.

Ensure the frontend API base URL points to the backend address and that
backend CORS configuration allows the frontend origin.
9. Environment Variables and Secrets
   Use environment variables or a secure secrets manager for credentials.
   Example variable names:
   DB_URL=jdbc:mysql://localhost:3306/e_commerce_system
   DB_USERNAME=your_database_username
   DB_PASSWORD=your_database_password
   BAKONG_ACCOUNT_ID=your_bakong_account_id
   BAKONG_API_TOKEN=your_bakong_api_token
   Do not put real values in this README. Do not commit .env files, API
   tokens, passwords, or private keys to Git.

If Docker Compose is used, pass secrets to the backend container through
an untracked local environment file or a production secrets mechanism.
Do not bake secrets into a Docker image.
10. Running with Docker
    If Dockerfiles and a Compose file are configured, build and start the
    services from the directory containing docker-compose.yml:
    docker compose up --build
    To stop the services:
    docker compose down
    Check the actual Compose service names, port mappings, volumes, health
    checks, and environment variable configuration in the repository before
    using these commands. Persist MySQL data with a named volume and keep
    Bakong credentials out of the image.
11. Testing
    Recommended checks before a release:
- Confirm the backend starts and connects to MySQL.
- Confirm the frontend can call the backend REST API.
- Test product browsing and category filtering.
- Test cart item creation, quantity updates, and removal.
- Test order creation and total calculation.
- Test delivery and order status updates.
- Test payment records for each supported payment method.
- Test Bakong with a valid KHQR payment and verify the transaction
  server-side.
- Confirm an unpaid or expired QR is never marked as paid.
- Test invalid input and unauthorized access to administrative
  operations.
  Use Postman or automated tests for backend API verification.
12. Future Improvements
- Add or improve authentication and role-based authorization.
- Add order notifications and delivery tracking.
- Improve product search, filtering, and stock management.
- Add automated tests for services and REST endpoints.
- Add API documentation with OpenAPI / Swagger.
- Add production logging, monitoring, backups, and deployment secrets
  management.
13. Project Learning Outcomes
    This project provides practical experience with:
- Full-stack web application development.
- Java and Spring Boot REST API development.
- React component-based frontend development.
- Relational database design and SQL.
- Layered backend architecture and separation of concerns.
- Payment gateway integration and transaction verification.
- Docker-based application packaging and deployment.
  Note: This README is a project-level draft based on the available
  project history and logs. Verify the exact package names, folder names,
  implemented endpoints, payment methods, and Docker service configuration
  against the current source code before publishing it.