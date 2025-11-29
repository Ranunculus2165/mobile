plugins {
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.5"
    java
}

group = "com.wheats"
version = "0.0.1-SNAPSHOT"

java {
    // 우리가 사용하는 자바 버전
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // 기본 Web (REST API)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // (나중에 DB 쓰면 여기 spring-boot-starter-data-jpa, driver 등 추가하면 됨)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
