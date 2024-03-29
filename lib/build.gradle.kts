import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "com.eliottgray"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Testing
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")

    implementation("com.uber:h3:3.7.0")
    implementation("com.googlecode.json-simple:json-simple:1.1.1") {
        // Testing framework junit which should be in Testing scope only, is incorrectly provided by json-simple.
        //  A vulnerability (CVE-2020-15250) exists in the current junit version, and the maintainer of the package
        //  has failed to update the package in a timely manner. To fix, let us remove the junit dependency.
        //  TODO: Replace json-simple with a package that is more-actively maintained.
        exclude(group = "junit", module = "junit")
    }
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")

    // Logging
    implementation("org.slf4j:slf4j-api:1.7.32")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
