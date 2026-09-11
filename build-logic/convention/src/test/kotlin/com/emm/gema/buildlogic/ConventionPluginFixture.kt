package com.emm.gema.buildlogic

import java.io.File
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

internal class ConventionPluginFixture(private val projectDirectory: File) {

    fun report(pluginIds: List<String>, androidConfiguration: String = ""): Map<String, String> {
        writeSettings()
        writeLocalProperties()
        writeStubModule("core/domain")
        writeStubModule("core/ui")
        writeProbeModule(pluginIds, androidConfiguration)

        val result: BuildResult = GradleRunner.create()
            .withProjectDir(projectDirectory)
            .withPluginClasspath()
            .withArguments(":probe:conventionReport", "-g", gradleUserHome, "--stacktrace")
            .build()

        return result.output
            .lineSequence()
            .filter { it.startsWith(REPORT_PREFIX) }
            .map { it.removePrefix(REPORT_PREFIX).substringBefore('=') to it.substringAfter('=') }
            .toMap()
    }

    private fun writeSettings() {
        val catalog: File = File(rootDirectory, "gradle/libs.versions.toml")
        write(
            "settings.gradle.kts",
            """
            dependencyResolutionManagement {
                repositories {
                    google()
                    mavenCentral()
                }
                versionCatalogs {
                    create("libs") {
                        from(files("${catalog.invariantSeparatorsPath}"))
                    }
                }
            }

            rootProject.name = "convention-fixture"
            include(":probe")
            include(":core:domain")
            include(":core:ui")
            """.trimIndent()
        )
    }

    private fun writeLocalProperties() {
        val androidHome: String = System.getenv("ANDROID_HOME")
            ?: System.getenv("ANDROID_SDK_ROOT")
            ?: File(System.getProperty("user.home"), "Library/Android/sdk").absolutePath
        write("local.properties", "sdk.dir=${File(androidHome).invariantSeparatorsPath}")
    }

    private fun writeStubModule(path: String) {
        write("$path/build.gradle.kts", "")
    }

    private fun writeProbeModule(pluginIds: List<String>, androidConfiguration: String) {
        val plugins: String = pluginIds.joinToString(separator = "\n") { """    id("$it")""" }
        val content: String = buildString {
            appendLine("plugins {")
            appendLine(plugins)
            appendLine("}")
            appendLine()
            appendLine(androidConfiguration)
            appendLine()
            append(REPORT_TASK)
        }
        write("probe/build.gradle.kts", content)
    }

    private fun write(path: String, content: String) {
        val target: File = File(projectDirectory, path)
        target.parentFile.mkdirs()
        target.writeText(content)
    }

    private companion object {

        const val REPORT_PREFIX: String = "REPORT "

        val rootDirectory: File = File(System.getProperty("gema.rootDir"))

        val gradleUserHome: String = File(System.getProperty("user.home"), ".gradle").absolutePath

        val REPORT_TASK: String = """
            tasks.register("conventionReport") {
                doLast {
                    val android = project.extensions.findByType(com.android.build.api.dsl.CommonExtension::class.java)
                    if (android != null) {
                        println("REPORT compileSdk=" + android.compileSdk)
                        println("REPORT minSdk=" + android.defaultConfig.minSdk)
                        println("REPORT sourceCompatibility=" + android.compileOptions.sourceCompatibility)
                        println("REPORT compose=" + android.buildFeatures.compose)
                        println("REPORT namespace=" + android.namespace)
                    }
                    val application = project.extensions.findByType(com.android.build.api.dsl.ApplicationExtension::class.java)
                    if (application != null) {
                        println("REPORT targetSdk=" + application.defaultConfig.targetSdk)
                        println("REPORT versionCode=" + application.defaultConfig.versionCode)
                        println("REPORT versionName=" + application.defaultConfig.versionName)
                        println("REPORT minifyRelease=" + application.buildTypes.getByName("release").isMinifyEnabled)
                    }
                    val java = project.extensions.findByType(org.gradle.api.plugins.JavaPluginExtension::class.java)
                    if (java != null) {
                        println("REPORT javaSourceCompatibility=" + java.sourceCompatibility)
                    }
                    val jvmTargets = project.tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile::class.java)
                        .map { it.compilerOptions.jvmTarget.get().target }
                        .distinct()
                        .sorted()
                    println("REPORT jvmTarget=" + jvmTargets.joinToString(separator = ","))
                    println("REPORT conventionTasks=" + project.tasks.names.filter { it == "testDebugUnitTest" || it == "checkSqlDelightSnapshots" }.sorted().joinToString(separator = ","))
                    println("REPORT projectDependencies=" + project.configurations.flatMap { it.dependencies }.filterIsInstance<org.gradle.api.artifacts.ProjectDependency>().map { it.path }.filter { it != project.path }.distinct().sorted().joinToString(separator = ","))
                }
            }
        """.trimIndent()
    }
}
