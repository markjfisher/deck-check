import org.gradle.kotlin.dsl.accessors.runtime.conventionPluginByName
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
}

val elderscrollsLegendsSdkJavaVersion: String by project
val cliktVersion: String by project
val kotlinVersion: String by project

val logbackClassicVersion: String by project
val logbackEncoderVersion: String by project
val junitJupiterEngineVersion: String by project
val assertJVersion: String by project
val mockkVersion: String by project
val okHttpVerison: String by project
val konfigVersion: String by project
val diskordVersion: String by project

// set in ~/.gradle/gradle.properties
val deckCheckBotToken: String by project

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("net.markjfisher:elderscrolls-legends-sdk-java:$elderscrollsLegendsSdkJavaVersion")
    implementation("com.github.ajalt:clikt:$cliktVersion")

    implementation("ch.qos.logback:logback-classic:$logbackClassicVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logbackEncoderVersion")
    implementation("com.natpryce:konfig:$konfigVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterEngineVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterEngineVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitJupiterEngineVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("com.squareup.okhttp3:okhttp:$okHttpVerison")

    implementation("com.jessecorbett:diskord:$diskordVersion")
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

    val deckCheckStartScripts by registering(CreateStartScripts::class) {
        group = "build"
        description = "Create run scripts for deck-check"
        outputDir = file("$buildDir/scripts")
        applicationName = "deck-check"

        val jarTask = project.tasks.getByName("jar")
        classpath = jarTask.outputs.files + configurations.getByName("runtime")
        dependsOn(jarTask)

        doLast {
            val adc = conventionPluginByName(convention, "application") as ApplicationPluginConvention
            adc.applicationDistribution.run {
                println("doing $name")
                into("bin") {
                    from(project.tasks[name])
                    fileMode = Integer.parseUnsignedInt("755", 8)
                }

            }
        }
    }

//    named<Zip>("distZip") {
//        val adc = conventionPluginByName(project.convention, "application") as ApplicationPluginConvention
//        adc.applicationDistribution.from(project.tasks.getByName("deckCheckStartScripts")) {
//            into("bin")
//        }
//    }

}

fun createScript(mainClass: String, name: String) {
    tasks.create(name = name, type = CreateStartScripts::class) {
        outputDir = file("$buildDir/scripts")
        mainClassName = mainClass
        applicationName = name
        classpath = tasks[JavaPlugin.JAR_TASK_NAME].outputs.files + configurations.getByName("runtime")
    }
    // val jarTask = tasks.getByName("jar")
    tasks[name].dependsOn(tasks.getByName("jar"))

    val adc = conventionPluginByName(convention, "application") as ApplicationPluginConvention
    adc.applicationDistribution.run {
        println("doing $name")
        into("bin") {
            from(project.tasks[name])
            fileMode = Integer.parseUnsignedInt("755", 8)
        }

    }
}

application {
    mainClassName = "legends.DeckCheck"
    application.applicationName = "deck-check"
    applicationDefaultJvmArgs = listOf("-Ddeck-check.bot.token=$deckCheckBotToken")
}

// apply(from = "$rootDir/../scripts.gradle.kts")
//tasks.getByName("startScripts").enabled = false
//tasks.getByName("run").enabled = false

// createScript(mainClass = "legends.DeckCheck", name = "deck-check")
// createScript(mainClass = "legends.BotCheck", name = "bot-check")
