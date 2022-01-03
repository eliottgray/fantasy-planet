[![CircleCI](https://circleci.com/gh/eliottgray/fantasy-planet/tree/main.svg?style=svg)](https://circleci.com/gh/eliottgray/fantasy-planet/tree/main)

# fantasy-planet
Generate, populate, and display procedurally-generated fantasy worlds. [A limited demo can be found here.](http://fantasy-planet.us-west-1.elasticbeanstalk.com/)

![Fantasy Planet Viewer](/fantasy-globe.png?raw=true "Fantasy planet viewer.")

# Requirements
* Java 11

# Building
* Project is built using the [Gradle Build Tool](https://gradle.org/), with a provided wrapper file.
* Gradle will self-install, requiring no specific action other than using the wrapper to perform a task, e.g. `./gradlew build`.

# Running
* Run the web application with the gradle application plugin: `./gradlew app:run`
* Application can then be interacted with at: http://127.0.0.1:5000/

# Packaging
* The [Shadow Gradle plugin](https://github.com/johnrengelman/shadow) can be used to package the application and dependencies together as a 'Fat' or 'Uber' Jar: `./gradlew app:shadowJar`.
* This Jar can then be deployed and run with `java -jar path/to/jar`.

# Demo Mode
* While the application is designed to generate worlds on request, in some instances limiting generation to a specific seed is desired.
* The following environment variables control activation of demo mode:
  * DEMO_MODE - defaults to `false`, set to `true` to enable limiting of world to a specific seed.
  * DEMO_SEED - controls the seed to which generation is limited.
* Example: `DEMO_MODE=true DEMO_SEED=98765 ./gradlew run`
