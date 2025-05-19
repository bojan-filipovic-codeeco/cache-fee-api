plugins {
    application // ← this makes `./gradlew run` honor your `mainClass`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktor)
    alias(libs.plugins.sonarqube)
}

group = "com.alsoug"
version = "0.0.1"

application {
    mainClass = "com.alsoug.ApplicationKt"
}

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.test.junit)

    // Logging
    implementation(libs.logback.classic)

    // Koin (DI)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)

    // Ktor Server
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.test.host)

    // Ktor Serialization
    implementation(libs.ktor.serialization.kotlinx.json)

    // Ktor HTTP Client (used with Restate or APIs)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)

    // Database
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.hikari)
    implementation(libs.postgresql)

    // Restate
    implementation(libs.restate.sdk)
    implementation(libs.restate.client.kotlin)
    implementation(libs.restate.serde.kotlinx)
    ksp(libs.restate.ksp)
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
        resources.srcDir("build/generated/ksp/main/resources")
    }

    jvmToolchain(17)
}

tasks.named<ProcessResources>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

sonarqube {
    properties {
        property("sonar.projectKey", "com.alsoug.cache-fee-api")
        property("sonar.projectName", "Cache Fee API")
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.login", "your-token-here")
        property("sonar.sources", "src/main/kotlin")
        property("sonar.java.binaries", listOf("build/classes/kotlin/main")) // must be a list
        property("sonar.java.source", "17")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.qualitygate.wait", "true") // optional, blocks build until quality gate is checked
        property("sonar.exclusions", "**/generated/**") // example: skip generated code
        property("sonar.inclusions", "**/*.kt") // focus on Kotlin sources
        property("sonar.gradle.skipCompile", "true") // prevent deprecated implicit compilation
    }
}
