# 1. Build Stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Run Stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render ke liye port expose karna
EXPOSE 8080

# App ko start karne ka command
ENTRYPOINT ["java", "-jar", "app.jar"]