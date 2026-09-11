package com.emm.gema.buildlogic

import app.cash.sqldelight.gradle.SqlDelightExtension
import java.io.File
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register

class SqlDelightConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("app.cash.sqldelight")

            val migrationDirectory: File = file("src/main/sqldelight/com/emm/gema/core/database")
            val snapshotDirectory: File = file("src/main/sqldelight/databases")

            extensions.configure<SqlDelightExtension> {
                databases.create("GemaDb") {
                    packageName.set("com.emm.gema.core.database")
                    schemaOutputDirectory.set(snapshotDirectory)
                    verifyMigrations.set(true)
                }
            }

            val checkSqlDelightSnapshots: TaskProvider<CheckSqlDelightSnapshotsTask> =
                tasks.register<CheckSqlDelightSnapshotsTask>("checkSqlDelightSnapshots") {
                    group = "verification"
                    description = "Fails when a migration and its schema snapshot do not come in pairs."
                    migrations.set(migrationDirectory)
                    snapshots.set(snapshotDirectory)
                }

            val migrationVerificationTasks: Spec<Task> = Spec {
                it.name.startsWith("verify") && it.name.endsWith("Migration")
            }

            tasks.matching(migrationVerificationTasks).configureEach {
                dependsOn(checkSqlDelightSnapshots)
            }
        }
    }
}
