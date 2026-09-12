package com.emm.gema.core.database.siagie

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.student.SqlDelightStudentRepository
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.siagie.ImportedTemplate
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.emm.gema.core.domain.siagie.SiagieImportStore
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.domain.student.StudentRepository
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

private val sectionId: SectionId = SectionId("section-1")
private val importedAt: Instant = Instant.parse("2026-09-10T12:00:00Z")

class SiagieImportPersistenceTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        .also { GemaDb.Schema.create(it) }
    private val database: GemaDb = GemaDb(driver)
    private val students: StudentRepository = SqlDelightStudentRepository(database, dispatcher)
    private val store: SiagieImportStore = SqlDelightSiagieImportStore(database, dispatcher)

    @Test
    fun `an applied import stores the students and the template together`() = runTest {
        store.apply(listOf(studentOf(firstStudentId, "10000000000001")), templateOf())

        assertThat(students.listBySection(sectionId).map { it.id }).containsExactly(firstStudentId)
        val stored: ImportedTemplate = requireNotNull(store.findTemplate(sectionId, ImportedTemplateKind.GRADES))
        assertThat(stored.fileName).isEqualTo("6 Primaria EBR.xlsx")
        assertThat(stored.content).isEqualTo(byteArrayOf(1, 2, 3))
        assertThat(stored.importedAt).isEqualTo(importedAt)
    }

    @Test
    fun `a failing import writes neither students nor template`() = runTest {
        rejectEveryTemplate()

        runCatching { store.apply(listOf(studentOf(firstStudentId, "10000000000001")), templateOf()) }

        assertThat(students.listBySection(sectionId)).isEmpty()
    }

    @Test
    fun `one transaction creates a new student and updates an existing one`() = runTest {
        store.apply(listOf(studentOf(firstStudentId, "10000000000001")), templateOf())

        store.apply(
            listOf(
                studentOf(firstStudentId, "10000000000001", "ALVARADO QUISPE, MARIA FERNANDA"),
                studentOf(secondStudentId, "10000000000002"),
            ),
            templateOf(),
        )

        val stored: List<Student> = students.listBySection(sectionId)
        assertThat(stored.map { it.id }).containsExactly(firstStudentId, secondStudentId)
        assertThat(stored.single { it.id == firstStudentId }.fullName)
            .isEqualTo("ALVARADO QUISPE, MARIA FERNANDA")
    }

    @Test
    fun `a second import replaces the template of the section`() = runTest {
        store.apply(emptyList(), templateOf())

        store.apply(emptyList(), templateOf(fileName = "3 Primaria EBR.xlsx"))

        val stored: ImportedTemplate = requireNotNull(store.findTemplate(sectionId, ImportedTemplateKind.GRADES))
        assertThat(stored.fileName).isEqualTo("3 Primaria EBR.xlsx")
    }

    @Test
    fun `the grades template and the attendance template live side by side`() = runTest {
        store.apply(emptyList(), templateOf())

        store.apply(emptyList(), templateOf(kind = ImportedTemplateKind.ATTENDANCE, fileName = "Asistencia.xls"))

        assertThat(store.findTemplate(sectionId, ImportedTemplateKind.GRADES)?.fileName)
            .isEqualTo("6 Primaria EBR.xlsx")
        assertThat(store.findTemplate(sectionId, ImportedTemplateKind.ATTENDANCE)?.fileName)
            .isEqualTo("Asistencia.xls")
    }

    @Test
    fun `clearing a section drops its imported templates`() = runTest {
        store.apply(emptyList(), templateOf())

        store.clearSection(sectionId)

        assertThat(store.findTemplate(sectionId, ImportedTemplateKind.GRADES)).isNull()
    }

    private fun rejectEveryTemplate() {
        driver.execute(
            identifier = null,
            sql = """
                CREATE TRIGGER reject_template BEFORE INSERT ON imported_template
                BEGIN SELECT RAISE(ABORT, 'the disk is full'); END
            """.trimIndent(),
            parameters = 0,
        )
    }

    private fun templateOf(
        kind: ImportedTemplateKind = ImportedTemplateKind.GRADES,
        fileName: String = "6 Primaria EBR.xlsx",
    ): ImportedTemplate = ImportedTemplate(
        sectionId = sectionId,
        kind = kind,
        fileName = fileName,
        content = byteArrayOf(1, 2, 3),
        importedAt = importedAt,
    )

    private fun studentOf(
        id: StudentId,
        code: String,
        fullName: String = "ALVARADO QUISPE, MARIA",
    ): Student = Student(
        id = id,
        sectionId = sectionId,
        code = StudentCode(code),
        fullName = fullName,
    )
}

private val firstStudentId: StudentId = StudentId("student-1")

private val secondStudentId: StudentId = StudentId("student-2")
