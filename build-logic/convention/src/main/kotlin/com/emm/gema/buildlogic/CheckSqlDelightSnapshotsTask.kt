package com.emm.gema.buildlogic

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class CheckSqlDelightSnapshotsTask : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:Optional
    abstract val migrations: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:Optional
    abstract val snapshots: DirectoryProperty

    @TaskAction
    fun check() {
        val migratedVersions: List<Int> = migrations.asFile.orNull.versionsOf(extension = "sqm")
        val snapshotVersions: List<Int> = snapshots.asFile.orNull.versionsOf(extension = "db")
        val baselineVersion: Int? = snapshotVersions.minOrNull()

        val missingSnapshots: List<String> = migratedVersions
            .filterNot { snapshotVersions.contains(it + 1) }
            .map { "$it.sqm has no snapshot ${it + 1}.db" }

        val orphanSnapshots: List<String> = snapshotVersions
            .filterNot { it == baselineVersion || migratedVersions.contains(it - 1) }
            .map { "$it.db has no migration ${it - 1}.sqm" }

        val problems: List<String> = missingSnapshots + orphanSnapshots
        check(problems.isEmpty()) {
            buildString {
                appendLine("SQLDelight migrations and schema snapshots do not match:")
                problems.forEach { appendLine("  $it") }
                appendLine("Every N.sqm ships its (N+1).db in the same commit, and no snapshot outlives its migration.")
                append("Generate a snapshot with ./gradlew :core:database:generateDebugGemaDbSchema")
            }
        }
    }

    private fun File?.versionsOf(extension: String): List<Int> = this
        ?.listFiles()
        .orEmpty()
        .filter { it.extension == extension }
        .mapNotNull { it.nameWithoutExtension.toIntOrNull() }
        .sorted()
}
