import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    id("kotlinx-serialization") version "1.3.30"
}

val elderscrollsLegendsSdkJavaVersion: String by project
val cliktVersion: String by project
val kotlinVersion: String by project

val logbackClassicVersion: String by project
val logbackEncoderVersion: String by project
val kotlinLoggingVersion: String by project
val junitJupiterEngineVersion: String by project
val assertJVersion: String by project
val mockkVersion: String by project
val okHttpVerison: String by project
val konfigVersion: String by project
val diskordVersion: String by project
val easyRulesCoreVersion: String by project

// set in ~/.gradle/gradle.properties
val deckCheckBotToken: String by project

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("net.markjfisher:elderscrolls-legends-sdk-java:$elderscrollsLegendsSdkJavaVersion")
    implementation("com.github.ajalt:clikt:$cliktVersion")

    implementation("ch.qos.logback:logback-classic:$logbackClassicVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logbackEncoderVersion")
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
    implementation("com.natpryce:konfig:$konfigVersion")

    implementation("org.jeasy:easy-rules-core:$easyRulesCoreVersion")
    implementation("org.jeasy:easy-rules-mvel:$easyRulesCoreVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterEngineVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterEngineVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitJupiterEngineVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("com.squareup.okhttp3:okhttp:$okHttpVerison")

    implementation("com.jessecorbett:diskord-jvm:$diskordVersion")
}

tasks {
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
    if (project.hasProperty("bot-check")) {
        mainClassName = "legends.BotCheck"
        application.applicationName = "bot-check"
        applicationDefaultJvmArgs = listOf("-Ddeck-check.bot.token=$deckCheckBotToken")
    } else {
        mainClassName = "legends.DeckCheck"
        application.applicationName = "deck-check"
        applicationDefaultJvmArgs = listOf("-Ddeck-check.bot.token=$deckCheckBotToken")
    }
}