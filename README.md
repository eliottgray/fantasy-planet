[![CircleCI](https://circleci.com/gh/eliottgray/fantasy-planet/tree/main.svg?style=svg)](https://circleci.com/gh/eliottgray/fantasy-planet/tree/main)

# fantasy-planet
Generate, populate, and display procedurally-generated fantasy worlds.

![Fantasy Planet Viewer](/fantasy-globe.png?raw=true "Fantasy planet viewer.")

# Requirements
* Java 11

# Building
* Project is built using Gradle, with a provided wrapper.
* Gradle will self-install, requiring no specific action other than using the wrapper, e.g. `./gradlew test`.

# Running
* Run the web application with the gradle application plugin: `./gradlew run`
* Application can then be interacted with at: http://127.0.0.1:8080/
