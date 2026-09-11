package com.emm.gema.core.database.backup

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.UuidIdGenerator
import com.emm.gema.core.database.schoolyear.SqlDelightSchoolYearRepository
import com.emm.gema.core.domain.backup.BackupFile
import com.emm.gema.core.domain.backup.BackupStore
import com.emm.gema.core.domain.backup.BackupValidation
import com.emm.gema.core.domain.backup.ValidateBackupUseCase
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class SqliteBackupStoreTest {

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `a fresh install restores the school years of the backed up database`() = runTest {
        val phone: DatabaseUnderTest = openDatabase("gema.db")
        phone.createSchoolYear(
            startDate = LocalDate.of(2026, 3, 2),
            endDate = LocalDate.of(2026, 12, 18),
            periodKind = PeriodKind.BIMESTER,
        )
        val backup: BackupFile = phone.store.writeSnapshot("gema-20260910-1432.gema")
        phone.close()

        val newPhone: DatabaseUnderTest = openDatabase("fresh.db")
        newPhone.store.replaceDatabase(File(backup.path).inputStream())
        newPhone.close()

        val restored: DatabaseUnderTest = openDatabase("fresh.db")
        val schoolYears: List<SchoolYear> = restored.schoolYears()

        assertThat(schoolYears).hasSize(1)
        assertThat(schoolYears.single().startDate).isEqualTo(LocalDate.of(2026, 3, 2))
        restored.close()
    }

    @Test
    fun `a written snapshot is a database this app accepts`() = runTest {
        val phone: DatabaseUnderTest = openDatabase("gema.db")
        val backup: BackupFile = phone.store.writeSnapshot("gema-20260910-1432.gema")
        phone.close()

        val validate = ValidateBackupUseCase(supportedSchemaVersion = GemaDb.Schema.version.toInt())
        val header: ByteArray = File(backup.path).readBytes().copyOfRange(0, HEADER_BYTES)

        assertThat(validate(header)).isInstanceOf(BackupValidation.Valid::class.java)
    }

    @Test
    fun `a snapshot is written inside the backup directory under the given name`() = runTest {
        val phone: DatabaseUnderTest = openDatabase("gema.db")

        val backup: BackupFile = phone.store.writeSnapshot("gema-20260910-1432.gema")

        assertThat(backup.name).isEqualTo("gema-20260910-1432.gema")
        assertThat(File(backup.path).parentFile).isEqualTo(phone.backupDirectory)
        phone.close()
    }

    @Test
    fun `a restore that breaks halfway leaves the current database untouched`() = runTest {
        val phone: DatabaseUnderTest = openDatabase("gema.db")
        phone.createSchoolYear(
            startDate = LocalDate.of(2026, 3, 2),
            endDate = LocalDate.of(2026, 12, 18),
            periodKind = PeriodKind.BIMESTER,
        )
        val store: BackupStore = phone.store
        phone.close()

        val failure: Result<Unit> = runCatching { store.replaceDatabase(BrokenInputStream()) }

        assertThat(failure.isFailure).isTrue()
        val reopened: DatabaseUnderTest = openDatabase("gema.db")
        assertThat(reopened.schoolYears()).hasSize(1)
        reopened.close()
    }

    private fun openDatabase(fileName: String): DatabaseUnderTest {
        val databaseFile = File(temporaryFolder.root, fileName)
        val backupDirectory = File(temporaryFolder.root, "backups")
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")
        if (!databaseFile.exists() || databaseFile.length() == 0L) {
            GemaDb.Schema.create(driver)
        }
        return DatabaseUnderTest(
            driver = driver,
            database = GemaDb(driver),
            backupDirectory = backupDirectory,
            store = SqliteBackupStore(
                driver = driver,
                databaseFile = databaseFile,
                backupDirectory = backupDirectory,
            ),
        )
    }

    private class DatabaseUnderTest(
        private val driver: SqlDriver,
        private val database: GemaDb,
        val backupDirectory: File,
        val store: BackupStore,
    ) {

        private val repository: SchoolYearRepository = SqlDelightSchoolYearRepository(
            database = database,
            dispatcher = UnconfinedTestDispatcher(),
        )

        suspend fun createSchoolYear(startDate: LocalDate, endDate: LocalDate, periodKind: PeriodKind) {
            repository.save(
                SchoolYear(
                    id = UuidIdGenerator().newId(),
                    label = startDate.year.toString(),
                    startDate = startDate,
                    endDate = endDate,
                    periodKind = periodKind,
                )
            )
        }

        suspend fun schoolYears(): List<SchoolYear> = repository.observeAll().first()

        fun close() {
            driver.close()
        }
    }

    private class BrokenInputStream : InputStream() {

        override fun read(): Int = throw IOException("the document went away")
    }

    private companion object {
        const val HEADER_BYTES = 100
    }
}
