import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.0"
}

group = "com.eliottgray"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    implementation("com.uber:h3:3.7.0")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")

    // Caching
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.4")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}
