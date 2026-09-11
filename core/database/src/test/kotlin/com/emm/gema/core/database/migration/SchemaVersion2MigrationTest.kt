package com.emm.gema.core.database.migration

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.domain.section.Area
import com.google.common.truth.Truth.assertThat
import org.junit.Test

private const val OLD_VERSION: Long = 2

class SchemaVersion2MigrationTest {

    @Test
    fun `a section written by schema two survives the migration`() {
        val driver: SqlDriver = createSchemaVersionTwo()

        driver.execute(
            identifier = null,
            sql = """
                INSERT INTO section (id, school_year_id, grade, name)
                VALUES ('section-1', 'year-1', 'THIRD', 'A')
            """.trimIndent(),
            parameters = 0,
        )

        GemaDb.Schema.migrate(driver, OLD_VERSION, GemaDb.Schema.version).value

        val sections = GemaDb(driver).sectionQueries.selectBySchoolYear("year-1").executeAsList()
        assertThat(sections.map { it.id }).containsExactly("section-1")
    }

    @Test
    fun `the migration adds the curriculum tables empty`() {
        val driver: SqlDriver = createSchemaVersionTwo()

        GemaDb.Schema.migrate(driver, OLD_VERSION, GemaDb.Schema.version).value

        val database = GemaDb(driver)
        assertThat(database.competencyQueries.selectAll().executeAsList()).isEmpty()
        assertThat(database.workedCompetencyQueries.selectBySectionAndPeriod("section-1", "period-1").executeAsList())
            .isEmpty()
    }

    @Test
    fun `the migrated competency table accepts the seed`() {
        val driver: SqlDriver = createSchemaVersionTwo()
        GemaDb.Schema.migrate(driver, OLD_VERSION, GemaDb.Schema.version).value
        val database = GemaDb(driver)

        database.competencyQueries.upsert(
            id = "PPSS-1",
            area = Area.PPSS.name,
            siagie_ordinal = 1,
            name = "Construye su identidad",
            curriculum_version = 1,
        )

        assertThat(database.competencyQueries.selectByArea(Area.PPSS.name).executeAsList()).hasSize(1)
    }

    private fun createSchemaVersionTwo(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        schemaVersionTwoStatements.forEach { driver.execute(identifier = null, sql = it, parameters = 0) }
        return driver
    }
}

private val schemaVersionTwoStatements: List<String> = listOf(
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
)
