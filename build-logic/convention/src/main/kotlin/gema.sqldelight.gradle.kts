import app.cash.sqldelight.gradle.SqlDelightExtension

plugins {
    id("app.cash.sqldelight")
}

val migrationDirectory: File = file("src/main/sqldelight/com/emm/gema/core/database")
val snapshotDirectory: File = file("src/main/sqldelight/databases")

extensions.configure<SqlDelightExtension> {
    databases {
        create("GemaDb") {
            packageName.set("com.emm.gema.core.database")
            schemaOutputDirectory.set(snapshotDirectory)
            verifyMigrations.set(true)
        }
    }
}

val checkSqlDelightSnapshots = tasks.register("checkSqlDelightSnapshots") {
    group = "verification"
    description = "Fails when a migration has no schema snapshot committed next to it."

    val migrations: File = migrationDirectory
    val snapshots: File = snapshotDirectory
    inputs.dir(migrations).withPropertyName("migrations").optional()
    inputs.dir(snapshots).withPropertyName("snapshots").optional()

    doLast {
        val versions: List<Int> = migrations.listFiles()
            .orEmpty()
            .filter { it.extension == "sqm" }
            .mapNotNull { it.nameWithoutExtension.toIntOrNull() }
            .sorted()

        val missing: List<String> = versions
            .filterNot { snapshots.resolve("${it + 1}.db").isFile }
            .map { "$it.sqm has no snapshot ${it + 1}.db" }

        check(missing.isEmpty()) {
            buildString {
                appendLine("Missing SQLDelight schema snapshots:")
                missing.forEach { appendLine("  $it") }
                appendLine("Every N.sqm ships its (N+1).db in the same commit.")
                append("Generate it with ./gradlew :core:database:generateDebugGemaDbSchema")
            }
        }
    }
}

tasks.matching { it.name == "verifySqlDelightMigration" || it.name.endsWith("GemaDbMigration") }
    .configureEach { dependsOn(checkSqlDelightSnapshots) }
