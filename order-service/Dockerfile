FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY pom.xml /workspace/pom.xml
COPY order-service/pom.xml /workspace/order-service/pom.xml
COPY order-service/src /workspace/order-service/src

RUN mvn -pl order-service -am -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/order-service/target/order-service-0.0.1-SNAPSHOT.jar /app/order-service.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/order-service.jar"]
