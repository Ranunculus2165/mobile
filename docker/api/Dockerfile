# 1단계: Gradle + JDK17 이미지로 빌드
FROM gradle:8.7-jdk17 AS builder

# 📌 컨테이너 안에서의 작업 디렉터리
WORKDIR /workspace/api

# 📌 빌드 컨텍스트(.) 기준으로 api 폴더 전체를 복사
#    (즉, 호스트의 WH_EATS/api -> 컨테이너 /workspace/api)
COPY api/ .

# 📌 여기(/workspace/api)에 build.gradle.kts, settings.gradle.kts, gradlew, src/ 가 있어야 함
RUN chmod +x ./gradlew && ./gradlew bootJar --no-daemon

# 2단계: 경량 JRE 이미지로 실행
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 위에서 빌드된 JAR 복사
COPY --from=builder /workspace/api/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
