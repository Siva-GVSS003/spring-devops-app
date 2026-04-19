# STAGE 1: Build stage
# We use a Maven image that already has Java + Maven installed
# No need to install anything manually!
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set working directory inside container
WORKDIR /app

# Copy pom.xml first (for dependency caching)
# Docker caches this layer — speeds up future builds!
COPY pom.xml .

# Download all dependencies
# This layer is cached unless pom.xml changes
RUN mvn dependency:go-offline

# Copy source codeg
COPY src ./src

# Build the JAR
RUN mvn clean package -DskipTests

# ---------------------------------------------------

# STAGE 2: Run stage
# We use a smaller image just to RUN the app
# No Maven needed here — just Java!
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy only the JAR from build stage
# This keeps our final image SMALL
COPY --from=build /app/target/spring-devops-app-1.0.jar app.jar

# Tell Docker our app runs on port 8080
EXPOSE 8080

# Command to run when container starts
ENTRYPOINT ["java", "-jar", "app.jar"]