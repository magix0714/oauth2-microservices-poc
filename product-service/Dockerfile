FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml /workspace/pom.xml
COPY api-gateway/pom.xml /workspace/api-gateway/pom.xml
COPY product-service/pom.xml /workspace/product-service/pom.xml
COPY order-service/pom.xml /workspace/order-service/pom.xml
COPY payment-service/pom.xml /workspace/payment-service/pom.xml
COPY product-service/src /workspace/product-service/src
RUN mvn -pl product-service -Dmaven.test.skip=true package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/product-service/target/product-service-0.0.1-SNAPSHOT.jar /app/product-service.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/product-service.jar"]
