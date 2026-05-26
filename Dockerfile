# ========== Build ==========
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Gradle Wrapper와 빌드 스크립트 먼저 복사 (캐싱 최적화)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# 의존성 다운로드 (캐싱)
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon || true

# 소스 코드 복사
COPY src src

# 빌드
RUN ./gradlew clean bootJar --no-daemon

# ========== Run ==========
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 시간대 설정 (한국 시간)
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

# 빌드 스테이지에서 만든 JAR만 가져오기
COPY --from=builder /app/build/libs/*.jar app.jar

# 8080 포트 노출
EXPOSE 8080

# JVM 옵션 (t3.small 2GB 메모리 고려)
ENV JAVA_OPTS="-Xms256m -Xmx768m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"

# 컨테이너 시작 시 실행할 명령
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]