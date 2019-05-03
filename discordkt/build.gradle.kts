import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

val kotlinVersion: String by project

val logbackClassicVersion: String by project
val logbackEncoderVersion: String by project
val junitJupiterEngineVersion: String by project
val assertJVersion: String by project
val mockkVersion: String by project
val okHttpVerison: String by project

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")

    implementation("ch.qos.logback:logback-classic:$logbackClassicVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logbackEncoderVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterEngineVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterEngineVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitJupiterEngineVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")

    implementation("com.squareup.okhttp3:okhttp:$okHttpVerison")

    implementation("com.squareup.retrofit2:retrofit:2.5.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.5.0")
    implementation("com.squareup.retrofit2:adapter-java8:2.5.0")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.9.8")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")

    // implementation("org.jetbrains.kotlin:kotlin-stdlib-jre8:1.1.60")
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:0.17")
    // implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation("org.apache.tika:tika-core:1.14")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.1.60")

}

tasks {
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
