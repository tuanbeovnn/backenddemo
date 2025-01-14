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

# Set the timezone for the container (optional)
RUN unlink /etc/localtime && ln -s /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime

# Copy the JAR file from the build stage
COPY --from=build /src/target/k8s-0.0.1-SNAPSHOT.jar /run/k8s-0.0.1-SNAPSHOT.jar

# Expose the port for the application
EXPOSE 8080

# Set the application properties file location (in case using an external volume or configmap)
ENTRYPOINT java -jar /run/k8s-0.0.1-SNAPSHOT.jar --spring.config.location=file:/run/src/main/resources/application.properties