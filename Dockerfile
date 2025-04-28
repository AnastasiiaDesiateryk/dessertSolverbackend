# Use the official Maven image to build the app
FROM maven:3.8.8-openjdk-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .

RUN mvn dependency:go-offline

# Copy the rest of the code
COPY src ./src

# Package the app
RUN mvn package

# Now create the actual running image
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the jar file from the build step
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
