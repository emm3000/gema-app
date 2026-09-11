package com.emm.gema.core.database.migration

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.emm.gema.core.database.GemaDb
import com.google.common.truth.Truth.assertThat
import org.junit.Test

private const val OLD_VERSION: Long = 4

class SchemaVersion4MigrationTest {

    @Test
    fun `a student written by schema four survives the migration`() {
        val driver: SqlDriver = createSchemaVersionFour()
        insertStudent(driver)

        GemaDb.Schema.migrate(driver, OLD_VERSION, GemaDb.Schema.version).value

        val students = GemaDb(driver).studentQueries.selectBySection("section-1").executeAsList()
        assertThat(students.map { it.full_name }).containsExactly("ACOSTA RIVERA, Luz Maria")
    }

    @Test
    fun `the migration adds the imported template table`() {
        val driver: SqlDriver = createSchemaVersionFour()

        GemaDb.Schema.migrate(driver, OLD_VERSION, GemaDb.Schema.version).value

        val database: GemaDb = GemaDb(driver)
        database.importedTemplateQueries.insert(
            section_id = "section-1",
            kind = "GRADES",
            file_name = "6 Primaria EBR.xlsx",
            content = byteArrayOf(1, 2, 3),
            imported_at = "2026-09-10T12:00:00Z",
        )

        val stored = database.importedTemplateQueries
            .selectBySectionAndKind("section-1", "GRADES")
            .executeAsOneOrNull()
        assertThat(stored?.file_name).isEqualTo("6 Primaria EBR.xlsx")
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

    private fun createSchemaVersionFour(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val statements: List<String> = schemaVersionThreeStatements + schemaVersionFourStatements
        statements.forEach { driver.execute(identifier = null, sql = it, parameters = 0) }
        return driver
    }
}

internal val schemaVersionFourStatements: List<String> = listOf(
    """
    CREATE TABLE student (
        id TEXT NOT NULL PRIMARY KEY,
        section_id TEXT NOT NULL,
        student_code TEXT NOT NULL,
        full_name TEXT NOT NULL,
        siagie_id TEXT,
        withdrawal_date TEXT
    )
    """.trimIndent(),
    "CREATE INDEX student_section_id ON student(section_id)",
    "CREATE UNIQUE INDEX student_section_code ON student(section_id, student_code)",
)
