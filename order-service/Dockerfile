FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=builder /app/target/order-service-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/data

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
