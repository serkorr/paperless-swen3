# Build the OCR worker using Maven and Java 25.
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

# Download dependencies before copying the source code.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compile and package the OCR worker.
COPY src ./src
RUN mvn clean package -DskipTests -B

# Run the worker with Java 25.
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/ocr-worker-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]