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
        mavenLocal()
        mavenCentral()
        jcenter()
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://kotlin.bintray.com/kotlinx")
    }

    group = "net.markjfisher"
    version = "1.1.14"

}
