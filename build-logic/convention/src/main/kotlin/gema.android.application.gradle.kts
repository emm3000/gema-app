import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.artifacts.VersionCatalog

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

extensions.configure<ApplicationExtension> {
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        targetSdk = 35
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

val libs: VersionCatalog = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    add("implementation", platform(libs.findLibrary("androidx-compose-bom").get()))
    add("implementation", libs.findLibrary("androidx-ui").get())
    add("implementation", libs.findLibrary("androidx-ui-graphics").get())
    add("implementation", libs.findLibrary("androidx-ui-tooling-preview").get())
    add("implementation", libs.findLibrary("androidx-material3").get())
    add("debugImplementation", libs.findLibrary("androidx-ui-tooling").get())
    add("testImplementation", libs.findLibrary("junit").get())
    add("testImplementation", libs.findLibrary("truth").get())
    add("testImplementation", libs.findLibrary("turbine").get())
    add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
}
