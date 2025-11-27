# 예시: Gradle로 빌드된 Spring Boot jar 기준
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# 필요 시 Gradle이나 빌드 툴 설치 과정 추가
COPY . .

# 여기서는 실제 빌드 명령은 나중에 채울 것
# RUN ./gradlew bootJar

FROM eclipse-temurin:17-jre

WORKDIR /app
# 빌드 결과물 복사 (실제 jar 이름에 맞게 수정 예정)
# COPY --from=build /app/build/libs/api.jar app.jar

EXPOSE 9000
CMD ["java", "-jar", "app.jar"]
