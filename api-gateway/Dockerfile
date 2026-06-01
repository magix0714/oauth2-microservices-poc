FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml /workspace/pom.xml
COPY api-gateway/pom.xml /workspace/api-gateway/pom.xml
COPY product-service/pom.xml /workspace/product-service/pom.xml
COPY order-service/pom.xml /workspace/order-service/pom.xml
COPY payment-service/pom.xml /workspace/payment-service/pom.xml
COPY api-gateway/src /workspace/api-gateway/src
RUN mvn -pl api-gateway -DskipTests -Djava.version=21 -Dmaven.compiler.release=21 package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar /app/api-gateway.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/api-gateway.jar"]
