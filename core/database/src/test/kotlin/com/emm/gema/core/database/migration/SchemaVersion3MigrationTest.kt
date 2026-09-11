package com.emm.gema.core.database.migration

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.emm.gema.core.database.GemaDb
import com.google.common.truth.Truth.assertThat
import org.junit.Test

private const val OLD_VERSION: Long = 3

class SchemaVersion3MigrationTest {

    @Test
    fun `a section written by schema three survives the migration`() {
        val driver: SqlDriver = createSchemaVersionThree()
        insertSection(driver)

        GemaDb.Schema.migrate(driver, OLD_VERSION, GemaDb.Schema.version).value

        val sections = GemaDb(driver).sectionQueries.selectBySchoolYear("year-1").executeAsList()
        assertThat(sections.map { it.name }).containsExactly("A")
    }

    @Test
    fun `the migration adds the student table`() {
        val driver: SqlDriver = createSchemaVersionThree()
        insertSection(driver)

        GemaDb.Schema.migrate(driver, OLD_VERSION, GemaDb.Schema.version).value

        val database: GemaDb = GemaDb(driver)
        assertThat(database.studentQueries.selectBySection("section-1").executeAsList()).isEmpty()

        database.studentQueries.insert(
            id = "student-1",
            section_id = "section-1",
            student_code = "12345678901234",
            full_name = "ACOSTA RIVERA, Luz Maria",
            siagie_id = null,
            withdrawal_date = null,
        )

        assertThat(database.studentQueries.selectBySection("section-1").executeAsList()).hasSize(1)
    }

    private fun insertSection(driver: SqlDriver) {
        driver.execute(
            identifier = null,
            sql = """
                INSERT INTO section (id, school_year_id, grade, name)
                VALUES ('section-1', 'year-1', 'THIRD', 'A')
            """.trimIndent(),
            parameters = 0,
        )
    }

    private fun createSchemaVersionThree(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        schemaVersionThreeStatements.forEach { driver.execute(identifier = null, sql = it, parameters = 0) }
        return driver
    }
}

private val schemaVersionThreeStatements: List<String> = listOf(
    """
    CREATE TABLE school_year (
        id TEXT NOT NULL PRIMARY KEY,
        start_date TEXT NOT NULL,
        end_date TEXT NOT NULL,
        period_kind TEXT NOT NULL,
        label TEXT NOT NULL DEFAULT ''
    )
    """.trimIndent(),
    """
    CREATE TABLE period (
        id TEXT NOT NULL PRIMARY KEY,
        school_year_id TEXT NOT NULL,
        number INTEGER NOT NULL,
        start_date TEXT NOT NULL,
        end_date TEXT NOT NULL
    )
    """.trimIndent(),
    "CREATE INDEX period_school_year_id ON period(school_year_id)",
    """
    CREATE TABLE section (
        id TEXT NOT NULL PRIMARY KEY,
        school_year_id TEXT NOT NULL,
        grade TEXT NOT NULL,
        name TEXT NOT NULL
    )
    """.trimIndent(),
    "CREATE INDEX section_school_year_id ON section(school_year_id)",
    """
    CREATE TABLE section_hidden_area (
        section_id TEXT NOT NULL,
        area TEXT NOT NULL,
        PRIMARY KEY (section_id, area)
    )
    """.trimIndent(),
    """
    CREATE TABLE active_school_year (
        id INTEGER NOT NULL PRIMARY KEY CHECK (id = 0),
        school_year_id TEXT NOT NULL
    )
    """.trimIndent(),
    """
    CREATE TABLE competency (
        id TEXT NOT NULL PRIMARY KEY,
        area TEXT NOT NULL,
        siagie_ordinal INTEGER NOT NULL,
        name TEXT NOT NULL,
        curriculum_version INTEGER NOT NULL
    )
    """.trimIndent(),
    "CREATE INDEX competency_area ON competency(area)",
    """
    CREATE TABLE worked_competency (
        section_id TEXT NOT NULL,
        period_id TEXT NOT NULL,
        competency_id TEXT NOT NULL,
        PRIMARY KEY (section_id, period_id, competency_id)
    )
    """.trimIndent(),
    "CREATE INDEX worked_competency_section_period ON worked_competency(section_id, period_id)",
)
