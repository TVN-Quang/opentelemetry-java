# Sử dụng image maven để build ứng dụng
FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Image chạy ứng dụng
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar service-a.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "service-a.jar"]
