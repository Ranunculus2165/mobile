plugins {
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.5"
    java
}

group = "com.wheats"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Web
    implementation("org.springframework.boot:spring-boot-starter-web")

    // JPA + Hibernate
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // ⭐ MySQL 드라이버 - 버전 X, Spring Boot BOM이 관리
    runtimeOnly("com.mysql:mysql-connector-j")

    // JWT 라이브러리
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.3")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
