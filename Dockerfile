## Build Stage ##
FROM maven:3.8.3-openjdk-17 as build

# Set working directory
WORKDIR /src

# Copy the entire project to the container
COPY . .

# Build the project, skipping tests
RUN mvn install -DskipTests=true

## Run Stage ##
FROM openjdk:17-alpine

# Set working directory for the run stage
WORKDIR /run

# Copy the JAR file from the build stage
COPY --from=build /src/target/k8s-0.0.1-SNAPSHOT.jar /run/k8s-0.0.1-SNAPSHOT.jar

# Copy application.properties to the /run directory
COPY --from=build /src/src/main/resources/application.yml /run/application.yml

# Expose the port for the application
EXPOSE 8083

# Set the entry point with the correct configuration location
ENTRYPOINT ["java", "-jar", "/run/k8s-0.0.1-SNAPSHOT.jar", "--spring.config.location=file:/run/application.properties"]
