# Docker Build Stage
FROM maven:3.8.3-openjdk-17 AS build

# Build Stage
WORKDIR /opt/app

COPY ./ /opt/app
RUN --mount=type=cache,target=/root/.m2 mvn clean install -DskipTests
#RUN mvn clean install -DskipTests

# Docker Runtime Stage
FROM openjdk:17-alpine

# Copy the built jar from the build stage
COPY --from=build /opt/app/target/*.jar app.jar

# Set environment variables
ENV PORT 8083
EXPOSE $PORT

# Specify the Spring config location (can be a path or a URL)
ENV SPRING_CONFIG_LOCATION=/config/application.properties

# Update ENTRYPOINT to include --spring.config.location
ENTRYPOINT ["java", "-jar", "-Xmx1024M", "-Dserver.port=${PORT}", "-Dspring.config.location=${SPRING_CONFIG_LOCATION}", "app.jar"]