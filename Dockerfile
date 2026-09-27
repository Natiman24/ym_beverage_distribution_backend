# First stage: Build the application.
FROM maven:3.8.4-openjdk-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

# Second stage: Create the final lightweight image
#FROM openjdk:17-jdk-alpine
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Accept an argument to specify the environment file
ARG ENV_FILE

# Copy the built JAR file
COPY --from=build /app/target/*.jar app.jar

# Copy the corresponding .env file
COPY ${ENV_FILE} .env

# Install envsubst (for processing .env variables)
RUN apk add --no-cache bash

# Set environment variables from .env file
CMD ["sh", "-c", "export $(grep -v '^#' .env | xargs) && java -jar app.jar"]
