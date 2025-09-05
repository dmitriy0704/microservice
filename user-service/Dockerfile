# ---------- Стадия сборки ----------
FROM gradle:8.7.0-jdk21 AS builder
WORKDIR /app

# Копируем wrapper и конфиги
COPY gradlew /app/
COPY gradle /app/gradle
COPY build.gradle.kts settings.gradle.kts /app/
RUN chmod +x ./gradlew

# Копируем весь проект (Gradle нужен контекст всех модулей)
COPY . .

# Сборка user-service
RUN ./gradlew :user-service:bootJar --no-daemon

# ---------- Стадия выполнения ----------
FROM eclipse-temurin:21-jdk-jammy AS runtime
WORKDIR /app

COPY --from=builder /app/user-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
