# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven project definition and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and package application
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Lightweight runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root group and user for security
RUN addgroup -S spendcheck && adduser -S spendcheck -G spendcheck
USER spendcheck

# Copy packaged jar from build stage
COPY --from=build --chown=spendcheck:spendcheck /app/target/*.jar app.jar

# Default port
ENV PORT=8080
EXPOSE ${PORT}

# Run the Spring Boot application, dynamically respecting the PORT env var
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

