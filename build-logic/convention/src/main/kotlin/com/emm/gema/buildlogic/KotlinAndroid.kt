package com.emm.gema.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension) {
    commonExtension.compileSdk = COMPILE_SDK
    commonExtension.defaultConfig.minSdk = MIN_SDK
    commonExtension.compileOptions.sourceCompatibility = JAVA_VERSION
    commonExtension.compileOptions.targetCompatibility = JAVA_VERSION

    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions.jvmTarget.set(JVM_TARGET)
        compilerOptions.optIn.add("kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
}

internal fun Project.configureAndroidUnitTestDependencies() {
    dependencies {
        add("testImplementation", libs.library("junit"))
        add("testImplementation", libs.library("truth"))
        add("testImplementation", libs.library("turbine"))
        add("testImplementation", libs.library("kotlinx-coroutines-test"))
    }
}
