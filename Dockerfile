# Stage 1: The "Builder" stage - where we compile our code
# We use a full JDK image here because we need the Java compiler and Maven
FROM maven:3.9-eclipse-temurin-17 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml first to leverage Docker's layer caching
# This layer only gets rebuilt if the dependencies change
COPY pom.xml .

# Copy the rest of the source code
COPY src ./src

# Build the application using Maven, creating the runnable JAR
# The "-DskipTests" flag is good practice for build pipelines
RUN mvn package -DskipTests

# ---

# Stage 2: The "Final" stage - where we run our application
# We use a lightweight JRE (Java Runtime Environment) image. It's smaller and more secure
# because it doesn't contain the compiler or other build tools.
FROM eclipse-temurin:17-jre-alpine

# Set the working directory for the final image
WORKDIR /app

# Copy ONLY the built .jar file from the "builder" stage into our final image
COPY --from=builder /app/target/*.jar ./app.jar

# Expose the port that our server listens on. This is for documentation and networking.
EXPOSE 5000

# The command to run when the container starts
CMD ["java", "-jar", "app.jar"]