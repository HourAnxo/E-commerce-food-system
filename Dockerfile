
# Backend: multi-stage build of the Spring Boot fat JAR.
# Build context is the repo root.

# --- build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
# Cache dependencies: resolve against pom.xml before copying sources.
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

# --- runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/*-SNAPSHOT.jar app.jar
EXPOSE 8081
# DB_URL / DB_USERNAME / DB_PASSWORD are supplied at runtime (see compose file).
ENTRYPOINT ["java", "-jar", "app.jar"]
