plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.detekt)
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        val moduleConfig = file("$rootDir/config/detekt/${project.name}.yml")
        val configFiles: List<File> = listOfNotNull(
            file("$rootDir/config/detekt/detekt.yml"),
            moduleConfig.takeIf { it.exists() }
        )
        config.setFrom(files(configFiles))
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

val androidPluginIds: Set<String> = setOf("com.android.library", "com.android.application")

abstract class CheckModuleBoundariesTask : DefaultTask() {

    @get:Input
    abstract val modulePaths: ListProperty<String>

    @get:Input
    abstract val moduleDependencies: MapProperty<String, List<String>>

    @get:Input
    abstract val jvmOnlyModulesWithAndroidPlugin: ListProperty<String>

    @TaskAction
    fun check() {
        val dependenciesByPath: Map<String, List<String>> = moduleDependencies.get()
        val violations: List<String> = buildList {
            modulePaths.get().forEach { path ->
                val dependencies: List<String> = dependenciesByPath.getValue(path)

                if (path != ":app") {
                    dependencies
                        .filter { it.startsWith(":feature:") }
                        .forEach { add("$path depends on $it; only :app may reach a feature module") }
                }

                if (path.startsWith(":core:")) {
                    dependencies
                        .filter { it.startsWith(":core:") && it != ":core:domain" }
                        .forEach { add("$path depends on $it; a core module may only reach :core:domain") }
                }
            }
            jvmOnlyModulesWithAndroidPlugin.get().forEach { path ->
                add("$path applies an Android plugin; it must stay JVM-only")
            }
        }
        check(violations.isEmpty()) {
            violations.joinToString(separator = "\n", prefix = "Module boundary violations:\n")
        }
    }
}

val checkModuleBoundaries: TaskProvider<CheckModuleBoundariesTask> = tasks.register<CheckModuleBoundariesTask>("checkModuleBoundaries") {
    group = "verification"
    description = "Fails when a module depends on a layer it is not allowed to reach."
}

gradle.projectsEvaluated {
    checkModuleBoundaries.configure {
        modulePaths.set(subprojects.map { it.path })
        moduleDependencies.set(
            subprojects.associate { module ->
                val dependencies: List<String> = module.configurations
                    .flatMap { it.dependencies }
                    .filterIsInstance<ProjectDependency>()
                    .map { it.path }
                    .filter { it != module.path }
                    .distinct()
                module.path to dependencies
            }
        )
        jvmOnlyModulesWithAndroidPlugin.set(
            subprojects
                .filter { it.path == ":core:domain" || it.path == ":core:siagie" }
                .filter { module -> androidPluginIds.any(module.plugins::hasPlugin) }
                .map { it.path }
        )
    }
}
