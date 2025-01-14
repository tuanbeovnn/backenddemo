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

# Copy the built jar from the build stage with the short name 'app.jar'
COPY --from=build /opt/app/target/*.jar /run/app.jar

# Set the environment variable for the port and expose it
ENV PORT 8083
EXPOSE $PORT

# Set spring config location for external application.properties
ENV SPRING_CONFIG_LOCATION=optional:file:/config/application.properties

# Start the application with the specified port
ENTRYPOINT ["java", "-jar", "-Xmx1024M", "-Dserver.port=${PORT}", "-Dspring.config.location=${SPRING_CONFIG_LOCATION}", "/run/app.jar"]
