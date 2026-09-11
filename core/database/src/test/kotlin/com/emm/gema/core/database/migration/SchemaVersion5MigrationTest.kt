package com.emm.gema.core.database.migration

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.Period_level
import com.emm.gema.core.database.Student
import com.google.common.truth.Truth.assertThat
import org.junit.Test

private const val OLD_VERSION: Long = 5

class SchemaVersion5MigrationTest {

    @Test
    fun `a student written by schema five survives the migration`() {
        val driver: SqlDriver = createSchemaVersionFive()
        insertStudent(driver)

        GemaDb.Schema.migrate(driver, OLD_VERSION, GemaDb.Schema.version).value

        val students: List<Student> = GemaDb(driver).studentQueries.selectBySection("section-1").executeAsList()
        assertThat(students.map { it.full_name }).containsExactly("ACOSTA RIVERA, Luz Maria")
    }

    @Test
    fun `the migration adds the period level table`() {
        val driver: SqlDriver = createSchemaVersionFive()
        insertStudent(driver)

        GemaDb.Schema.migrate(driver, OLD_VERSION, GemaDb.Schema.version).value

        val database: GemaDb = GemaDb(driver)
        assertThat(database.periodLevelQueries.selectByPeriod("section-1", "period-1").executeAsList()).isEmpty()

        database.periodLevelQueries.upsert(
            section_id = "section-1",
            period_id = "period-1",
            student_id = "student-1",
            competency_id = "PPSS-1",
            achievement_level = "C",
            unworked_comment = null,
            descriptive_conclusion = "",
        )

        val stored: List<Period_level> =
            database.periodLevelQueries.selectByPeriod("section-1", "period-1").executeAsList()
        assertThat(stored).hasSize(1)
        assertThat(stored.single().achievement_level).isEqualTo("C")
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

    private fun createSchemaVersionFive(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val statements: List<String> =
            schemaVersionThreeStatements + schemaVersionFourStatements + schemaVersionFiveStatements
        statements.forEach { driver.execute(identifier = null, sql = it, parameters = 0) }
        return driver
    }
}

private val schemaVersionFiveStatements: List<String> = listOf(
    """
    CREATE TABLE imported_template (
        section_id TEXT NOT NULL,
        kind TEXT NOT NULL,
        file_name TEXT NOT NULL,
        content BLOB NOT NULL,
        imported_at TEXT NOT NULL,
        PRIMARY KEY (section_id, kind)
    )
    """.trimIndent(),
)
