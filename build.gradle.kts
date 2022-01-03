plugins {
    // Resolving the plugin here, but not applying, resolves warning:
    //
    // The Kotlin Gradle plugin was loaded multiple times in different subprojects, which is not supported and may break the build.
    // This might happen in subprojects that apply the Kotlin plugins with the Gradle 'plugins { ... }' DSL if they specify explicit versions, even if the versions are equal.
    // Please add the Kotlin plugin to the common parent project or the root project, then remove the versions in the subprojects.
    // If the parent project does not need the plugin, add 'apply false' to the plugin line.
    // See: https://docs.gradle.org/current/userguide/plugins.html#sec:subprojects_plugins_dsl
    kotlin("jvm") version "1.6.10" apply false

}