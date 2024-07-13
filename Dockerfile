FROM openjdk:17-jdk AS build
WORKDIR /app
COPY . /app
RUN chmod +x ./gradlew

RUN microdnf install -y findutils
RUN ./gradlew bootJar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/build/libs/cheq-0.0.4-SNAPSHOT.jar"]