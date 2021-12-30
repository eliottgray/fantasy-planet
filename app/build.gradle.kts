import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.0"
    application
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "com.eliottgray"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")

    // Web Frameworks  // TODO: Break out ktor version into global project variable.
    implementation("io.ktor:ktor-server-core:1.6.7")
    implementation("io.ktor:ktor-server-netty:1.6.7")
    implementation("io.ktor:ktor-html-builder:1.6.7")
    testImplementation("io.ktor:ktor-server-test-host:1.6.7")
    implementation("ch.qos.logback:logback-classic:1.2.5")

    implementation(project(":lib"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("com.eliottgray.kotlin.ApplicationKt")
}

tasks{
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "com.eliottgray.kotlin.ApplicationKt"))
        }
    }
}
