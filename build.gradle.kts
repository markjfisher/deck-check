import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    id("com.github.ben-manes.versions")
}

group = "net.markjfisher"
version = "1.1.0"

val elderscrollsLegendsSdkJavaVersion: String by project
val cliktVersion: String by project
val kotlinVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("net.markjfisher:elderscrolls-legends-sdk-java:$elderscrollsLegendsSdkJavaVersion")
    implementation("com.github.ajalt:clikt:$cliktVersion")
}

tasks {
    getByName<Wrapper>("wrapper") {
        gradleVersion = "5.4"
        distributionType = Wrapper.DistributionType.ALL
    }

    named<JavaExec>("run") {
        jvmArgs(listOf("-noverify", "-XX:TieredStopAtLevel=1"))
    }

    named<KotlinCompile>("compileKotlin") {
        kotlinOptions {
            jvmTarget = "1.8"
            javaParameters = true
        }
    }

    named<KotlinCompile>("compileTestKotlin") {
        kotlinOptions {
            jvmTarget = "1.8"
            javaParameters = true
        }
    }

    withType<Test> {
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
    }

    named<Test>("test") {
        useJUnitPlatform()
    }

}

application {
    mainClassName = "legends.DeckCheck"
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
}
