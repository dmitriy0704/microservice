plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.13") // Or a newer version
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13")
}