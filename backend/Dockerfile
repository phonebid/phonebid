FROM gradle:8.7-jdk17 AS builder

WORKDIR /workspace

# 캐시 최적화: 의존성 메타데이터 먼저 복사
COPY gradlew settings.gradle build.gradle /workspace/
COPY gradle /workspace/gradle

# 소스 복사
COPY src /workspace/src

RUN chmod +x /workspace/gradlew \
  && ./gradlew clean bootJar --no-daemon


FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

ENV TZ=Asia/Seoul
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

