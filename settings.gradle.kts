rootProject.name = "deck-check"

val kotlinVersion: String by settings
val gradleVersionsVersion: String by settings

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when(requested.id.id) {
                "org.jetbrains.kotlin.jvm" -> useVersion(kotlinVersion)
                "com.github.ben-manes.versions" -> useVersion(gradleVersionsVersion)
            }
        }
    }
}