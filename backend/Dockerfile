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

# Spring Profile을 환경변수로 받아서 실행
# docker-compose의 env_file에서 SPRING_PROFILES_ACTIVE를 주입
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

