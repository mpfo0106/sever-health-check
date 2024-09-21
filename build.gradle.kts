plugins {
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.5"
    // allOpen + default 생성자
    kotlin("plugin.jpa") version "1.9.24"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
    kotlin("kapt") version "2.0.0"
}

version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

val dotenvKotlinVersion = "6.4.1"
val swaggerVersion = "2.3.0"
val slackVersion = "1.40.2"
val coroutinesVersion = "1.6.4"
val mockitoVersion = "5.4.0"


repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    //dotenv
    implementation("io.github.cdimascio:dotenv-kotlin:$dotenvKotlinVersion")
    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$swaggerVersion")
    //webSocket
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    // slack
    implementation("com.slack.api:slack-api-client:$slackVersion")
    // slack kotlin
    implementation("com.slack.api:slack-api-model-kotlin-extension:$slackVersion")
    implementation("com.slack.api:slack-api-client-kotlin-extension:$slackVersion")
    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
    // queryDsl
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    implementation("com.querydsl:querydsl-apt:5.0.0:jakarta")
    implementation("jakarta.persistence:jakarta.persistence-api")
    implementation("jakarta.annotation:jakarta.annotation-api")

    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
    kapt("org.springframework.boot:spring-boot-configuration-processor")

    //Lombok
    implementation("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")


    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.mysql:mysql-connector-j")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoVersion")
    // coroutines test
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${coroutinesVersion}")

    // h2
    testImplementation("com.h2database:h2")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}