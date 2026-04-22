FROM gradle:8.5-jdk17 AS build
WORKDIR /home/gradle/project

COPY build.gradle settings.gradle ./
COPY src src

RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-jammy AS prepare
WORKDIR /workspace/app

COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN useradd -ms /bin/bash fhiruser
USER fhiruser

COPY --from=prepare /workspace/app/dependencies/ ./
COPY --from=prepare /workspace/app/spring-boot-loader/ ./
COPY --from=prepare /workspace/app/snapshot-dependencies/ ./
COPY --from=prepare /workspace/app/application/ ./

EXPOSE 8085

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "org.springframework.boot.loader.launch.JarLauncher"]