import org.gradle.api.artifacts.VersionCatalog
plugins {
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
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
    testImplementation(libs.findLibrary("kotlinx-coroutines-test").get())
}

tasks.withType<Test>().configureEach {
    useJUnit()
}

tasks.register("testDebugUnitTest") {
    group = "verification"
    description = "Runs the unit tests of this JVM module under the name the Android modules use."
    dependsOn(tasks.named("test"))
}
