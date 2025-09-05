# ---------- Стадия сборки ----------
FROM gradle:8.7.0-jdk21 AS builder
WORKDIR /app

COPY gradlew /app/
COPY gradle /app/gradle
COPY build.gradle.kts settings.gradle.kts /app/
RUN chmod +x ./gradlew

COPY . .

# Сборка order-service
RUN ./gradlew :order-service:bootJar --no-daemon

# ---------- Стадия выполнения ----------
FROM eclipse-temurin:21-jdk-jammy AS runtime
WORKDIR /app

COPY --from=builder /app/order-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
