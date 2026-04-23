# Use Eclipse Temurin JDK 17
FROM eclipse-temurin:17-jdk-jammy AS build

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY server/build.gradle server/

# Copy source code
COPY server/src server/src

# Make gradlew executable
RUN chmod +x gradlew

# Build with limited memory for free tier
RUN ./gradlew :server:build -x test --no-daemon --max-workers=1 -Dorg.gradle.jvmargs="-Xmx512m -XX:MaxMetaspaceSize=256m"

# Use runtime image
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the built jar
COPY --from=build /app/server/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run with limited memory
ENTRYPOINT ["java", "-Xmx400m", "-jar", "app.jar"]

