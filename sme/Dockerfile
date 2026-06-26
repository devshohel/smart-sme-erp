FROM maven:3.9.14-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN mkdir -p /app/logs /app/uploads/products /app/uploads/expense-receipts
COPY --from=build /workspace/target/*.jar /app/smart-sme-erp.jar
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/smart-sme-erp.jar"]
