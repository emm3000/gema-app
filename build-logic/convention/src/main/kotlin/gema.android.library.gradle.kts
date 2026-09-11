import org.gradle.api.artifacts.VersionCatalog
import com.android.build.gradle.LibraryExtension

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

extensions.configure<LibraryExtension> {
    namespace = "com.emm.gema" + path.replace(':', '.')
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

val libs: VersionCatalog = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    testImplementation(libs.findLibrary("junit").get())
    testImplementation(libs.findLibrary("truth").get())
    testImplementation(libs.findLibrary("turbine").get())
    testImplementation(libs.findLibrary("kotlinx-coroutines-test").get())
}
