# Этап сборки
FROM gradle:8.7-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon

# Этап рантайма
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
#
#
#FROM eclipse-temurin:21-jdk AS build
#WORKDIR /app
#COPY . .
#RUN ./gradlew clean bootJar --no-daemon
#
#FROM eclipse-temurin:21-jre
#WORKDIR /app
#COPY --from=build /app/build/libs/*.jar app.jar
#ENTRYPOINT ["java", "-jar", "app.jar"]


##

# Используем официальный образ OpenJDK 17 как базовый
#FROM openjdk:17-jdk-slim
#
# Устанавливаем рабочую директорию в контейнере
#WORKDIR /app
#
# Копируем файл сборки Gradle
#COPY build.gradle.kts .
#COPY settings.gradle.kts .
#COPY gradlew .
#COPY gradle gradle

# Копируем исходный код
#COPY src src

# Даем права на выполнение для gradlew
#RUN chmod +x gradlew

# Собираем приложение
#RUN ./gradlew build

# Копируем собранный JAR-файл
#COPY build/libs/*.jar app.jar

# Указываем порт, который будет использовать приложение
#EXPOSE 8080

# Команда для запуска приложения
#ENTRYPOINT ["java", "-jar", "app.jar"]