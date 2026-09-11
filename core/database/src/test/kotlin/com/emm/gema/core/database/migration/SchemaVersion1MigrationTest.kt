package com.emm.gema.core.database.migration

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.emm.gema.core.database.GemaDb
import com.google.common.truth.Truth.assertThat
import org.junit.Test

private const val OLD_VERSION: Long = 1

class SchemaVersion1MigrationTest {

    @Test
    fun `a school year written by schema one survives the migration`() {
        val driver: SqlDriver = createSchemaVersionOne()

        driver.execute(
            identifier = null,
            sql = """
                INSERT INTO school_year (id, start_date, end_date, period_kind)
                VALUES ('year-1', '2026-03-02', '2026-12-18', 'BIMESTER')
            """.trimIndent(),
            parameters = 0,
        )

        GemaDb.Schema.migrate(driver, OLD_VERSION, GemaDb.Schema.version).value

        val schoolYears = GemaDb(driver).schoolYearQueries.selectAll().executeAsList()
        assertThat(schoolYears).hasSize(1)
        assertThat(schoolYears.single().id).isEqualTo("year-1")
        assertThat(schoolYears.single().period_kind).isEqualTo("BIMESTER")
    }

    @Test
    fun `a school year written by schema one is labelled with its starting year`() {
        val driver: SqlDriver = createSchemaVersionOne()

        driver.execute(
            identifier = null,
            sql = """
                INSERT INTO school_year (id, start_date, end_date, period_kind)
                VALUES ('year-1', '2026-03-02', '2026-12-18', 'BIMESTER')
            """.trimIndent(),
            parameters = 0,
        )

        GemaDb.Schema.migrate(driver, OLD_VERSION, GemaDb.Schema.version).value

        assertThat(GemaDb(driver).schoolYearQueries.selectAll().executeAsOne().label).isEqualTo("2026")
    }

    @Test
    fun `the migration adds the tables the setup flow writes`() {
        val driver: SqlDriver = createSchemaVersionOne()

        GemaDb.Schema.migrate(driver, OLD_VERSION, GemaDb.Schema.version).value

        val database = GemaDb(driver)
        assertThat(database.periodQueries.selectBySchoolYear("year-1").executeAsList()).isEmpty()
        assertThat(database.sectionQueries.selectBySchoolYear("year-1").executeAsList()).isEmpty()
        assertThat(database.sectionHiddenAreaQueries.selectBySection("section-1").executeAsList()).isEmpty()
        assertThat(database.activeSchoolYearQueries.select().executeAsOneOrNull()).isNull()
    }

    private fun createSchemaVersionOne(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        driver.execute(
            identifier = null,
            sql = """
                CREATE TABLE school_year (
                    id TEXT NOT NULL PRIMARY KEY,
                    start_date TEXT NOT NULL,
                    end_date TEXT NOT NULL,
                    period_kind TEXT NOT NULL
                )
            """.trimIndent(),
            parameters = 0,
        )
        return driver
    }
}
