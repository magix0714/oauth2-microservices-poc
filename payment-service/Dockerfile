FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY pom.xml /workspace/pom.xml
COPY payment-service/pom.xml /workspace/payment-service/pom.xml
COPY payment-service/src /workspace/payment-service/src

RUN mvn -pl payment-service -am -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/payment-service/target/payment-service-0.0.1-SNAPSHOT.jar /app/payment-service.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/payment-service.jar"]
