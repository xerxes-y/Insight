# 1st Docker build stage: build the project with Gradle
FROM gradle:7-jdk16 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon
EXPOSE 8888
COPY --from=build /home/gradle/src/build/libs/*.jar /app/Insight-1.0.0-SNAPSHOT-fat.jar
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/Insight-1.0.0-SNAPSHOT-fat.jar"]
