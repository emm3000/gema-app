import org.gradle.api.artifacts.VersionCatalog
plugins {
    id("gema.android.compose")
}

val libs: VersionCatalog = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    add("implementation", project(":core:domain"))
    add("implementation", project(":core:ui"))
    add("implementation", platform(libs.findLibrary("koin-bom").get()))
    add("implementation", libs.findLibrary("koin-core").get())
    add("implementation", libs.findLibrary("koin-androidx-compose").get())
    add("implementation", libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
}
