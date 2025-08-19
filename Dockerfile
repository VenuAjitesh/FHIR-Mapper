FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

COPY build.gradle settings.gradle ./
RUN gradle buildEnvironment --no-daemon

COPY . .
RUN gradle build --no-daemon -x test

FROM eclipse-temurin:17-jre-focal
WORKDIR /app

COPY --from=builder /app/build/libs/*-SNAPSHOT.jar fhir.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "fhir.jar"]
