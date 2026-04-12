FROM gradle:8.7-jdk17 AS builder

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle /workspace/
COPY gradle /workspace/gradle

COPY src /workspace/src

RUN chmod +x /workspace/gradlew \
  && ./gradlew clean bootJar --no-daemon


FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime \
    && apk del tzdata

WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

ENV TZ=Asia/Seoul \
    JAVA_OPTS="-Xms256m -Xmx384m -XX:+UseContainerSupport -XX:MaxMetaspaceSize=128m -Djava.io.tmpdir=/tmp"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
