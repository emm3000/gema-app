package com.emm.gema.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(commonExtension: CommonExtension) {
    commonExtension.buildFeatures.compose = true

    dependencies {
        add("implementation", platform(libs.library("androidx-compose-bom")))
        add("implementation", libs.library("androidx-ui"))
        add("implementation", libs.library("androidx-ui-graphics"))
        add("implementation", libs.library("androidx-ui-tooling-preview"))
        add("implementation", libs.library("androidx-material3"))
        add("debugImplementation", libs.library("androidx-ui-tooling"))
    }
}
