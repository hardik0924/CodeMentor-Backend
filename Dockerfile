# Use OpenJDK 21 as base image
FROM eclipse-temurin:21-jdk-jammy

# Set working directory inside the container
WORKDIR /app

# Copy the JAR file from target/ to the container
COPY target/CodeMentorBackend-0.0.1-SNAPSHOT.jar app.jar

# Expose port (adjust if your Spring Boot app uses a different port)
EXPOSE 8080

# Command to run your app
ENTRYPOINT ["java", "-jar", "app.jar"]
