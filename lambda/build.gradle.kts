plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "com.eliottgray"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib"))

    implementation ("com.amazonaws:aws-lambda-java-core:1.2.1")
    implementation ("com.amazonaws:aws-lambda-java-events:3.11.0")
    runtimeOnly ("com.amazonaws:aws-lambda-java-log4j2:1.5.1")

    implementation("com.google.code.gson:gson:2.7")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks{
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "com.eliottgray.kotlin.ApplicationKt"))
        }
    }
}

tasks.register<Zip>("buildzip") {
    from("compileJava")
    from("processResources")

    from(layout.buildDirectory.dir("toArchive")) {
        include("**/*.pdf")
        into("docs")
    }
}

tasks.named<Zip>("buildzip") {
    from("compileJava")
    from("processResources")

    into("foob")
}
