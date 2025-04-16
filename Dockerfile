FROM openjdk:17-jdk AS build
WORKDIR /app
COPY . /app
RUN chmod +x ./gradlew && ./gradlew bootJar

FROM openjdk:17-jdk
WORKDIR /app
COPY --from=build /app/build/libs/gamzaWeb-0.0.1-SNAPSHOT.jar /app/app.jar
COPY /home/jenkins/application.yml /tmp/application.yml

ENTRYPOINT ["java", "-Dspring.config.location=file:/tmp/application.yml", "-jar", "/app/app.jar"]
