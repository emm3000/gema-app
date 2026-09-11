plugins {
    `kotlin-dsl`
}

group = "com.emm.gema.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.compose.compiler.gradle.plugin)
    implementation(libs.sqldelight.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "gema.android.application"
            implementationClass = "com.emm.gema.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "gema.android.library"
            implementationClass = "com.emm.gema.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "gema.android.compose"
            implementationClass = "com.emm.gema.buildlogic.AndroidComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "gema.android.feature"
            implementationClass = "com.emm.gema.buildlogic.AndroidFeatureConventionPlugin"
        }
        register("androidRelease") {
            id = "gema.android.release"
            implementationClass = "com.emm.gema.buildlogic.AndroidReleaseConventionPlugin"
        }
        register("jvmLibrary") {
            id = "gema.jvm.library"
            implementationClass = "com.emm.gema.buildlogic.JvmLibraryConventionPlugin"
        }
        register("sqldelight") {
            id = "gema.sqldelight"
            implementationClass = "com.emm.gema.buildlogic.SqlDelightConventionPlugin"
        }
    }
}
