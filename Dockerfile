# Use Eclipse Temurin JDK 17 as base image
FROM eclipse-temurin:17-jdk-alpine AS build

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY server/build.gradle server/
COPY server/pom.xml server/

# Copy source code
COPY server/src server/src

# Make gradlew executable and convert line endings
RUN chmod +x gradlew && dos2unix gradlew || sed -i 's/\r$//' gradlew

# Build the application (skip tests for faster build) with Java 17
RUN ./gradlew :server:build -x test --no-daemon -Dorg.gradle.java.home=/opt/java/openjdk

# Use a smaller JRE image for runtime
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/server/build/libs/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Set environment variable for port
ENV PORT=8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

