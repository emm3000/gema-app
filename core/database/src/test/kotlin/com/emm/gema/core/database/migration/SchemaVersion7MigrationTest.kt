package com.emm.gema.core.database.migration

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.SelectByPeriod
import com.emm.gema.core.database.Student
import com.google.common.truth.Truth.assertThat
import org.junit.Test

private const val OLD_VERSION: Long = 7

class SchemaVersion7MigrationTest {

    @Test
    fun `a student written by schema seven survives the migration`() {
        val driver: SqlDriver = createSchemaVersionSeven()
        insertStudent(driver)

        GemaDb.Schema.migrate(driver, OLD_VERSION, GemaDb.Schema.version).value

        val students: List<Student> = GemaDb(driver).studentQueries.selectBySection("section-1").executeAsList()
        assertThat(students.map { it.full_name }).containsExactly("ACOSTA RIVERA, Luz Maria")
    }

    @Test
    fun `the migration adds the activity and evidence level tables`() {
        val driver: SqlDriver = createSchemaVersionSeven()
        insertStudent(driver)

        GemaDb.Schema.migrate(driver, OLD_VERSION, GemaDb.Schema.version).value

        val database: GemaDb = GemaDb(driver)
        assertThat(database.activityQueries.selectByPeriod("section-1", "period-1").executeAsList()).isEmpty()

        database.activityQueries.upsert(
            id = "activity-1",
            section_id = "section-1",
            period_id = "period-1",
            name = "Debate del aula",
            date = "2026-06-22",
        )
        database.activityQueries.insertCompetency(activity_id = "activity-1", competency_id = "PPSS-1")
        database.evidenceLevelQueries.upsert(
            activity_id = "activity-1",
            student_id = "student-1",
            competency_id = "PPSS-1",
            achievement_level = "B",
        )

        val storedActivities: List<SelectByPeriod> =
            database.activityQueries.selectByPeriod("section-1", "period-1").executeAsList()
        assertThat(storedActivities).hasSize(1)
        assertThat(storedActivities.single().competency_ids).isEqualTo("PPSS-1")

        val storedEvidence = database.evidenceLevelQueries.selectByActivity("activity-1").executeAsList()
        assertThat(storedEvidence).hasSize(1)
        assertThat(storedEvidence.single().achievement_level).isEqualTo("B")
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

    private fun createSchemaVersionSeven(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val statements: List<String> = schemaVersionThreeStatements +
            schemaVersionFourStatements +
            schemaVersionFiveStatements +
            schemaVersionSevenBaselineStatements
        statements.forEach { driver.execute(identifier = null, sql = it, parameters = 0) }
        return driver
    }
}

private val schemaVersionSevenBaselineStatements: List<String> = listOf(
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
)
