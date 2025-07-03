# Build stage
FROM openjdk:17-jdk AS build

RUN microdnf install -y findutils

WORKDIR /app
COPY . /app
RUN chmod +x ./gradlew && ./gradlew bootJar

FROM openjdk:17-jdk

WORKDIR /app
COPY --from=build /app/build/libs/gamzaWeb-0.0.1-SNAPSHOT.jar /app/app.jar

# ENTRYPOINT ["java", "-Dspring.config.additional-location=file:/tmp/", "-jar", "/app/app.jar"]
ENTRYPOINT ["java", "-Dspring.config.location=file:/tmp/application.yml", "-jar", "/app/app.jar"]

