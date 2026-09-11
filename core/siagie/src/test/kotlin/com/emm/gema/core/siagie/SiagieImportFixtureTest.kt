package com.emm.gema.core.siagie

import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.siagie.ApplySiagieImportUseCase
import com.emm.gema.core.domain.siagie.ImportedTemplate
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.emm.gema.core.domain.siagie.PreviewSiagieImportUseCase
import com.emm.gema.core.domain.siagie.SiagieImportPlan
import com.emm.gema.core.domain.siagie.SiagieImportPlanner
import com.emm.gema.core.domain.siagie.SiagieImportPreview
import com.emm.gema.core.domain.siagie.SiagieImportRejection
import com.emm.gema.core.domain.siagie.SiagieImportResult
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.google.common.truth.Truth.assertThat
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

private val sectionId: SectionId = SectionId("section-1")
private const val FIXTURE_NAME: String = "6 Primaria EBR.xlsx"
private val withdrawalDate: LocalDate = LocalDate.of(2026, 9, 10)

class SiagieImportFixtureTest {

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    private val sections = FakeSectionRepository()
    private val students = FakeStudentRepository()
    private val store = FakeSiagieImportStore(students)
    private val documents = FileSiagieDocuments()
    private val planner = SiagieImportPlanner(sections, students, documents, XlsxSiagieRosterReader())
    private val previewImport = PreviewSiagieImportUseCase(planner)
    private val applyImport = ApplySiagieImportUseCase(
        planner = planner,
        students = students,
        store = store,
        idGenerator = SequentialIdGenerator(),
        clock = Clock.fixed(Instant.parse("2026-09-10T12:00:00Z"), ZoneOffset.UTC),
    )

    @Test
    fun `the first import of a real template creates the whole section`() = runTest {
        sections.section = sectionOf(Grade.SIXTH)

        val preview: SiagieImportPreview = previewImport(sectionId, fixture().absolutePath)
        val result: SiagieImportResult = applyImport(sectionId, fixture().absolutePath, emptySet(), withdrawalDate)

        val plan: SiagieImportPlan = (preview as SiagieImportPreview.Ready).plan
        assertThat(plan.fileName).isEqualTo(FIXTURE_NAME)
        assertThat(plan.created).hasSize(5)
        assertThat(result).isEqualTo(SiagieImportResult.Applied(created = 5, updated = 0, withdrawn = 0))
        assertThat(students.listBySection(sectionId).map { it.fullName })
            .contains("ALVARADO QUISPE, MARIA FERNANDA")
    }

    @Test
    fun `the imported file is stored for the export that comes later`() = runTest {
        sections.section = sectionOf(Grade.SIXTH)

        applyImport(sectionId, fixture().absolutePath, emptySet(), withdrawalDate)

        val template: ImportedTemplate =
            requireNotNull(store.findTemplate(sectionId, ImportedTemplateKind.GRADES))
        assertThat(template.fileName).isEqualTo(FIXTURE_NAME)
        assertThat(template.content).isEqualTo(fixture().readBytes())
    }

    @Test
    fun `importing the same template twice creates nobody`() = runTest {
        sections.section = sectionOf(Grade.SIXTH)
        applyImport(sectionId, fixture().absolutePath, emptySet(), withdrawalDate)

        val result: SiagieImportResult = applyImport(sectionId, fixture().absolutePath, emptySet(), withdrawalDate)

        assertThat(result).isEqualTo(SiagieImportResult.Applied(created = 0, updated = 0, withdrawn = 0))
        assertThat(students.listBySection(sectionId)).hasSize(5)
    }

    @Test
    fun `a re-import brings the names siagie changed`() = runTest {
        sections.section = sectionOf(Grade.SIXTH)
        applyImport(sectionId, fixture().absolutePath, emptySet(), withdrawalDate)
        val renamed: File = fixtureWith("C4", "ALVARADO QUISPE, MARIA F.")

        val result: SiagieImportResult = applyImport(sectionId, renamed.absolutePath, emptySet(), withdrawalDate)

        assertThat(result).isEqualTo(SiagieImportResult.Applied(created = 0, updated = 1, withdrawn = 0))
        assertThat(students.listBySection(sectionId).map { it.fullName })
            .contains("ALVARADO QUISPE, MARIA F.")
    }

    @Test
    fun `a student absent from the template is proposed as withdrawn and kept`() = runTest {
        sections.section = sectionOf(Grade.SIXTH)
        students.save(
            Student(
                id = StudentId("student-typed"),
                sectionId = sectionId,
                code = StudentCode("99999999999999"),
                fullName = "TORRES PINO, LUIS",
            )
        )

        val preview: SiagieImportPreview = previewImport(sectionId, fixture().absolutePath)
        val plan: SiagieImportPlan = (preview as SiagieImportPreview.Ready).plan
        val result: SiagieImportResult =
            applyImport(sectionId, fixture().absolutePath, setOf(StudentId("student-typed")), withdrawalDate)

        assertThat(plan.missing.map { it.fullName }).containsExactly("TORRES PINO, LUIS")
        assertThat(result).isEqualTo(SiagieImportResult.Applied(created = 5, updated = 0, withdrawn = 1))
        val kept: Student = requireNotNull(students.findById(StudentId("student-typed")))
        assertThat(kept.withdrawalDate).isEqualTo(withdrawalDate)
    }

    @Test
    fun `a template of another grade is rejected before anything is written`() = runTest {
        sections.section = sectionOf(Grade.THIRD)

        val preview: SiagieImportPreview = previewImport(sectionId, fixture().absolutePath)

        assertThat(preview).isEqualTo(
            SiagieImportPreview.Rejected(SiagieImportRejection.GradeMismatch(expected = 3, found = 6))
        )
        assertThat(students.listBySection(sectionId)).isEmpty()
    }

    @Test
    fun `a roster broken in the middle is rejected instead of silently truncated`() = runTest {
        sections.section = sectionOf(Grade.SIXTH)
        val broken: File = fixtureWith("B6", "")

        val preview: SiagieImportPreview = previewImport(sectionId, broken.absolutePath)

        assertThat(preview).isEqualTo(SiagieImportPreview.Rejected(SiagieImportRejection.MalformedRow(row = 6)))
        assertThat(students.listBySection(sectionId)).isEmpty()
    }

    @Test
    fun `a file that is not a template is rejected`() = runTest {
        sections.section = sectionOf(Grade.SIXTH)
        val notATemplate: File = temporaryFolder.newFile("notas.xlsx")
        notATemplate.writeText("these are my notes")

        val preview: SiagieImportPreview = previewImport(sectionId, notATemplate.absolutePath)

        assertThat(preview).isEqualTo(SiagieImportPreview.Rejected(SiagieImportRejection.NotASiagieTemplate))
    }

    private fun sectionOf(grade: Grade): Section =
        Section(id = sectionId, schoolYearId = SchoolYearId("year-1"), grade = grade, name = "A")

    private fun fixtureWith(reference: String, text: String): File {
        val target: File = File(temporaryFolder.newFolder(), FIXTURE_NAME)
        XlsxTemplate(fixture()).fill(target, mapOf("COMU" to mapOf(reference to text)))
        return target
    }

    private fun fixture(): File = File(requireNotNull(javaClass.classLoader.getResource(FIXTURE_NAME)).toURI())
}
