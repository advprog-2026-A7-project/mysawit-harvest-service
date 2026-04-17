# ---- Build stage ----
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /home/gradle/project
COPY . .
RUN gradle --no-daemon clean build -x test

# ---- Run stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
