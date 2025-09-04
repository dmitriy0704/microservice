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
