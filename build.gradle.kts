plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.4" apply false
    alias(libs.plugins.detekt)
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        val moduleConfig = file("$rootDir/config/detekt/${project.name}.yml")
        config.setFrom(
            files(
                "$rootDir/config/detekt/detekt.yml",
                moduleConfig.takeIf { it.exists() }
            )
        )
        baseline = file("$projectDir/detekt-baseline.xml")
        buildUponDefaultConfig = true
        parallel = true
        basePath = rootDir.absolutePath
    }

    dependencies {
        detektPlugins(rootProject.libs.detekt.formatting)
        detektPlugins(rootProject.libs.detekt.compose.rules)
    }
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
    basePath = rootDir.absolutePath
}