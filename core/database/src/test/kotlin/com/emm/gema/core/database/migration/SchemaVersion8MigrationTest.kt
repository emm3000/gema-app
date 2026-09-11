package com.emm.gema.core.database.migration

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.emm.gema.core.database.Evidence_level as EvidenceLevelRow
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.Student
import com.google.common.truth.Truth.assertThat
import org.junit.Test

private const val OLD_VERSION: Long = 8

class SchemaVersion8MigrationTest {

    @Test
    fun `a student written by schema eight survives the migration`() {
        val driver: SqlDriver = createSchemaVersionEight()
        insertStudent(driver)

        GemaDb.Schema.migrate(driver, OLD_VERSION, GemaDb.Schema.version).value

        val students: List<Student> = GemaDb(driver).studentQueries.selectBySection("section-1").executeAsList()
        assertThat(students.map { it.full_name }).containsExactly("ACOSTA RIVERA, Luz Maria")
    }

    @Test
    fun `an evidence level written by schema eight survives the migration`() {
        val driver: SqlDriver = createSchemaVersionEight()
        insertStudent(driver)
        insertActivity(driver)
        driver.execute(
            identifier = null,
            sql = """
                INSERT INTO evidence_level (activity_id, student_id, competency_id, achievement_level)
                VALUES ('activity-1', 'student-1', 'PPSS-1', 'B')
            """.trimIndent(),
            parameters = 0,
        )

        GemaDb.Schema.migrate(driver, OLD_VERSION, GemaDb.Schema.version).value

        val stored: List<EvidenceLevelRow> =
            GemaDb(driver).evidenceLevelQueries.selectByActivity("activity-1").executeAsList()
        assertThat(stored.single().achievement_level).isEqualTo("B")
    }

    @Test
    fun `the migration allows an explicit no-evidence mark`() {
        val driver: SqlDriver = createSchemaVersionEight()
        insertStudent(driver)
        insertActivity(driver)

        GemaDb.Schema.migrate(driver, OLD_VERSION, GemaDb.Schema.version).value

        val database: GemaDb = GemaDb(driver)
        database.evidenceLevelQueries.upsert(
            activity_id = "activity-1",
            student_id = "student-1",
            competency_id = "PPSS-1",
            achievement_level = null,
        )

        val stored: List<EvidenceLevelRow> = database.evidenceLevelQueries.selectByActivity("activity-1")
            .executeAsList()
        assertThat(stored).hasSize(1)
        assertThat(stored.single().achievement_level).isNull()
    }

    private fun insertStudent(driver: SqlDriver) {
        driver.execute(
            identifier = null,
            sql = """
                INSERT INTO student (id, section_id, student_code, full_name, siagie_id, withdrawal_date)
                VALUES ('student-1', 'section-1', '12345678901234', 'ACOSTA RIVERA, Luz Maria', NULL, NULL)
            """.trimIndent(),
            parameters = 0,
        )
    }

    private fun insertActivity(driver: SqlDriver) {
        driver.execute(
            identifier = null,
            sql = """
                INSERT INTO activity (id, section_id, period_id, name, date)
                VALUES ('activity-1', 'section-1', 'period-1', 'Debate del aula', '2026-06-22')
            """.trimIndent(),
            parameters = 0,
        )
        driver.execute(
            identifier = null,
            sql = """
                INSERT INTO activity_competency (activity_id, competency_id)
                VALUES ('activity-1', 'PPSS-1')
            """.trimIndent(),
            parameters = 0,
        )
    }

    private fun createSchemaVersionEight(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val statements: List<String> = schemaVersionThreeStatements +
            schemaVersionFourStatements +
            schemaVersionFiveStatements +
            schemaVersionEightBaselineStatements
        statements.forEach { driver.execute(identifier = null, sql = it, parameters = 0) }
        return driver
    }
}

private val schemaVersionEightBaselineStatements: List<String> = listOf(
    """
    CREATE TABLE period_level (
        section_id TEXT NOT NULL,
        period_id TEXT NOT NULL,
        student_id TEXT NOT NULL,
        competency_id TEXT NOT NULL,
        achievement_level TEXT,
        unworked_comment TEXT,
        descriptive_conclusion TEXT NOT NULL DEFAULT ''
    )
    """.trimIndent(),
    "CREATE INDEX period_level_section_period ON period_level(section_id, period_id)",
    """
    CREATE TABLE attendance (
        section_id TEXT NOT NULL,
        student_id TEXT NOT NULL,
        date TEXT NOT NULL,
        status TEXT NOT NULL,
        PRIMARY KEY (student_id, date)
    )
    """.trimIndent(),
    "CREATE INDEX attendance_section_date ON attendance(section_id, date)",
    """
    CREATE TABLE activity (
        id TEXT NOT NULL PRIMARY KEY,
        section_id TEXT NOT NULL,
        period_id TEXT NOT NULL,
        name TEXT NOT NULL,
        date TEXT NOT NULL
    )
    """.trimIndent(),
    "CREATE INDEX activity_section_period ON activity(section_id, period_id)",
    """
    CREATE TABLE activity_competency (
        activity_id TEXT NOT NULL,
        competency_id TEXT NOT NULL,
        PRIMARY KEY (activity_id, competency_id)
    )
    """.trimIndent(),
    """
    CREATE TABLE evidence_level (
        activity_id TEXT NOT NULL,
        student_id TEXT NOT NULL,
        competency_id TEXT NOT NULL,
        achievement_level TEXT NOT NULL,
        PRIMARY KEY (activity_id, student_id, competency_id)
    )
    """.trimIndent(),
    "CREATE INDEX evidence_level_activity ON evidence_level(activity_id)",
)
