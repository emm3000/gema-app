package com.emm.gema.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

class AndroidFeatureConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("gema.android.compose")

            dependencies {
                add("implementation", project(":core:domain"))
                add("implementation", project(":core:ui"))
                add("implementation", platform(libs.library("koin-bom")))
                add("implementation", libs.library("koin-core"))
                add("implementation", libs.library("koin-androidx-compose"))
                add("implementation", libs.library("androidx-material-icons-extended"))
                add("implementation", libs.library("androidx-lifecycle-runtime-ktx"))
                add("implementation", libs.library("androidx-lifecycle-runtime-compose"))
                add("implementation", libs.library("androidx-lifecycle-viewmodel-ktx"))
            }
        }
    }
}
