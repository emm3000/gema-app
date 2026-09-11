import org.gradle.api.artifacts.VersionCatalog
import com.android.build.api.dsl.LibraryExtension

plugins {
    id("gema.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

extensions.configure<LibraryExtension> {
    buildFeatures {
        compose = true
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
}
