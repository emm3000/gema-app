package com.emm.gema.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class JvmLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")

            configureKotlinJvm()

            dependencies {
                add("testImplementation", libs.library("junit"))
                add("testImplementation", libs.library("truth"))
                add("testImplementation", libs.library("kotlinx-coroutines-test"))
            }

            tasks.register("testDebugUnitTest") {
                group = "verification"
                description = "Runs the unit tests of this JVM module under the name the Android modules use."
                dependsOn(tasks.named("test"))
            }
        }
    }
}
