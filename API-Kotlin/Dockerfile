# Use a Maven image to build the application
FROM maven:3.8.5-openjdk-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project files to the container
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Use a smaller base image to run the application
# FROM openjdk:17-jdk-slim
FROM openjdk:17.0.2-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the built jar file from the previous stage
COPY --from=build /app/target/*.jar app.jar

# TODO Use PostgreSQL
# Copy the H2 database files
# COPY database ./database

# Set environment variable
ENV JWT_KEY=xxx

# Expose the port that the application runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
