plugins {
    id("com.github.ben-manes.versions")
    kotlin("jvm") apply false
}

tasks {
    getByName<Wrapper>("wrapper") {
        gradleVersion = "5.4"
        distributionType = Wrapper.DistributionType.ALL
    }
}

defaultTasks(
        "clean", "build"
)

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        maven(url = "https://plugins.gradle.org/m2/")
    }

    group = "net.markjfisher"
    version = "1.1.0"

}
