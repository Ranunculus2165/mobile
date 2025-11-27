FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
# RUN ./gradlew bootJar

FROM eclipse-temurin:17-jre
WORKDIR /app
# COPY --from=build /app/build/libs/oauth-server.jar app.jar

EXPOSE 9100
CMD ["java", "-jar", "app.jar"]

