rootProject.name = "deck-check-app"

include(
    "cli"
)

val kotlinVersion: String by settings
val kotlinXSerialisationVersion: String by settings
val gradleVersionsVersion: String by settings

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when(requested.id.id) {
                "org.jetbrains.kotlin.jvm" -> useVersion(kotlinVersion)
                "com.github.ben-manes.versions" -> useVersion(gradleVersionsVersion)
                "kotlinx-serialization" -> {
                    useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
                }
            }
        }
    }
}
