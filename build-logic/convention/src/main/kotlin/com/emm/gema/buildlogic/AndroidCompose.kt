package com.emm.gema.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

internal fun Project.configureAndroidCompose(commonExtension: CommonExtension) {
    commonExtension.buildFeatures.compose = true
    commonExtension.testOptions.unitTests.isIncludeAndroidResources = true

    dependencies {
        add("implementation", platform(libs.library("androidx-compose-bom")))
        add("implementation", libs.library("androidx-ui"))
        add("implementation", libs.library("androidx-ui-graphics"))
        add("implementation", libs.library("androidx-ui-tooling-preview"))
        add("implementation", libs.library("androidx-material3"))
        add("debugImplementation", libs.library("androidx-ui-tooling"))
        add("testImplementation", platform(libs.library("androidx-compose-bom")))
        add("testImplementation", libs.library("androidx-compose-ui-test-junit4"))
        add("testImplementation", libs.library("robolectric"))
        add("debugImplementation", platform(libs.library("androidx-compose-bom")))
        add("debugImplementation", libs.library("androidx-compose-ui-test-manifest"))
    }

    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions.optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
    }
}
