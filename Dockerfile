# Step 1: Build the Spring Boot application
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests


# Step 2: Run the application
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/target/*.jar bookapi.jar

EXPOSE 8080

CMD ["java", "-jar", "bookapi.jar"]