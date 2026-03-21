# Build stage
FROM gradle:8-jdk17-alpine AS build
WORKDIR /app

# Copy Gradle files
COPY build.gradle settings.gradle ./

# Download dependencies (cached if build files unchanged)
RUN gradle dependencies --no-daemon || true

# Copy source and build
COPY src src
RUN gradle bootJar --no-daemon -x test

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN adduser -D -g '' appuser
USER appuser

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
