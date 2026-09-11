package com.emm.gema.core.database.migration

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.emm.gema.core.database.Attendance
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.Student
import com.google.common.truth.Truth.assertThat
import org.junit.Test

private const val OLD_VERSION: Long = 6

class SchemaVersion6MigrationTest {

    @Test
    fun `a student written by schema six survives the migration`() {
        val driver: SqlDriver = createSchemaVersionSix()
        insertStudent(driver)

        GemaDb.Schema.migrate(driver, OLD_VERSION, GemaDb.Schema.version).value

        val students: List<Student> = GemaDb(driver).studentQueries.selectBySection("section-1").executeAsList()
        assertThat(students.map { it.full_name }).containsExactly("ACOSTA RIVERA, Luz Maria")
    }

    @Test
    fun `the migration adds the attendance table`() {
        val driver: SqlDriver = createSchemaVersionSix()
        insertStudent(driver)

        GemaDb.Schema.migrate(driver, OLD_VERSION, GemaDb.Schema.version).value

        val database: GemaDb = GemaDb(driver)
        assertThat(database.attendanceQueries.selectBySectionAndDate("section-1", "2026-09-10").executeAsList())
            .isEmpty()

        database.attendanceQueries.insert(
            section_id = "section-1",
            student_id = "student-1",
            date = "2026-09-10",
            status = "LATE",
        )

        val stored: List<Attendance> =
            database.attendanceQueries.selectBySectionAndDate("section-1", "2026-09-10").executeAsList()
        assertThat(stored).hasSize(1)
        assertThat(stored.single().status).isEqualTo("LATE")
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

    private fun createSchemaVersionSix(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val statements: List<String> = schemaVersionThreeStatements +
            schemaVersionFourStatements +
            schemaVersionFiveStatements +
            schemaVersionSixStatements
        statements.forEach { driver.execute(identifier = null, sql = it, parameters = 0) }
        return driver
    }
}

private val schemaVersionSixStatements: List<String> = listOf(
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
)
