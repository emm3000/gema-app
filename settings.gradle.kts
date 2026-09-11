pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Gema"
include(":app")
include(":core:domain")
include(":core:database")
include(":core:siagie")
include(":core:ui")
include(":feature:setup")
include(":feature:sections")
include(":feature:students")
include(":feature:attendance")
include(":feature:evaluation")
include(":feature:export")
include(":feature:backup")
