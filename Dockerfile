# Docker Build Stage
FROM maven:3.8.3-openjdk-17 AS build

# Set the working directory
WORKDIR /opt/app

# Copy application source code to the container
COPY ./ /opt/app

# Build the project, skipping tests to speed up the build
RUN --mount=type=cache,target=/root/.m2 mvn clean install -DskipTests

# Docker Runtime Stage
FROM openjdk:17-alpine

# Copy the built jar from the build stage
COPY --from=build /opt/app/target/*.jar app.jar

# Copy the application.properties file into the container
COPY ./src/main/resources/application.properties /config/application.properties

# Set the environment variable for the port and expose it
ENV PORT 8083
EXPOSE $PORT

# Start the application with the specified port and config location
ENTRYPOINT ["java", "-jar", "-Xmx1024M", "-Dserver.port=${PORT}", "-Dspring.config.location=file:/config/application.properties", "app.jar"]