package com.emm.gema.core.domain.siagie

import com.emm.gema.core.domain.fake.InMemorySectionRepository
import com.emm.gema.core.domain.fake.InMemorySiagieImportStore
import com.emm.gema.core.domain.fake.InMemoryStudentRepository
import com.emm.gema.core.domain.fake.SequentialIdGenerator
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Test

private val sectionId: SectionId = SectionId("section-1")
private const val URI: String = "content://documents/6-primaria.xlsx"
private const val FILE_NAME: String = "6 Primaria EBR.xlsx"
private const val FIRST_CODE: String = "10000000000001"
private const val SECOND_CODE: String = "10000000000002"
private val withdrawalDate: LocalDate = LocalDate.of(2026, 9, 10)

class SiagieImportUseCasesTest {

    private val sections = InMemorySectionRepository()
    private val students = InMemoryStudentRepository()
    private val store = InMemorySiagieImportStore(students)
    private val documents = FakeSiagieDocuments(FILE_NAME, content)
    private val reader = FakeSiagieRosterReader()
    private val planner = SiagieImportPlanner(sections, students, documents, reader)
    private val previewImport = PreviewSiagieImportUseCase(planner)
    private val applyImport = ApplySiagieImportUseCase(
        planner = planner,
        students = students,
        store = store,
        idGenerator = SequentialIdGenerator("student"),
        clock = Clock.fixed(Instant.parse("2026-09-10T12:00:00Z"), ZoneOffset.UTC),
    )

    @Test
    fun `the first import creates every student of the template`() = runTest {
        givenSection()
        reader.roster = rosterOf(
            entry(FIRST_CODE, "ALVARADO QUISPE, MARIA"),
            entry(SECOND_CODE, "BAUTISTA HUAMAN, JOSE"),
        )

        val preview: SiagieImportPreview = previewImport(sectionId, URI)

        val plan: SiagieImportPlan = (preview as SiagieImportPreview.Ready).plan
        assertThat(plan.fileName).isEqualTo(FILE_NAME)
        assertThat(plan.created.map { it.fullName })
            .containsExactly("ALVARADO QUISPE, MARIA", "BAUTISTA HUAMAN, JOSE")
        assertThat(plan.updated).isEmpty()
        assertThat(plan.missing).isEmpty()
    }

    @Test
    fun `applying the import stores the students with their siagie id`() = runTest {
        givenSection()
        reader.roster = rosterOf(entry(FIRST_CODE, "ALVARADO QUISPE, MARIA"))

        val result: SiagieImportResult = applyImport(sectionId, URI, emptySet(), withdrawalDate)

        assertThat(result).isEqualTo(SiagieImportResult.Applied(created = 1, updated = 0, withdrawn = 0))
        val stored: Student = students.listBySection(sectionId).single()
        assertThat(stored.code).isEqualTo(StudentCode(FIRST_CODE))
        assertThat(stored.fullName).isEqualTo("ALVARADO QUISPE, MARIA")
        assertThat(stored.siagieId).isEqualTo("1001")
    }

    @Test
    fun `applying the import stores the original file for the section`() = runTest {
        givenSection()
        reader.roster = rosterOf(entry(FIRST_CODE, "ALVARADO QUISPE, MARIA"))

        applyImport(sectionId, URI, emptySet(), withdrawalDate)

        val template: ImportedTemplate = requireNotNull(store.findTemplate(sectionId, ImportedTemplateKind.GRADES))
        assertThat(template.fileName).isEqualTo(FILE_NAME)
        assertThat(template.content).isEqualTo(content)
    }

    @Test
    fun `importing the same file twice changes nothing`() = runTest {
        givenSection()
        reader.roster = rosterOf(entry(FIRST_CODE, "ALVARADO QUISPE, MARIA"))
        applyImport(sectionId, URI, emptySet(), withdrawalDate)
        val before: List<Student> = students.listBySection(sectionId)

        val preview: SiagieImportPreview = previewImport(sectionId, URI)
        val result: SiagieImportResult = applyImport(sectionId, URI, emptySet(), withdrawalDate)

        assertThat((preview as SiagieImportPreview.Ready).plan.created).isEmpty()
        assertThat(preview.plan.updated).isEmpty()
        assertThat(result).isEqualTo(SiagieImportResult.Applied(created = 0, updated = 0, withdrawn = 0))
        assertThat(students.listBySection(sectionId)).isEqualTo(before)
    }

    @Test
    fun `a re-import updates the name of a student that changed in siagie`() = runTest {
        givenSection()
        reader.roster = rosterOf(entry(FIRST_CODE, "ALVARADO QUISPE, MARIA"))
        applyImport(sectionId, URI, emptySet(), withdrawalDate)
        reader.roster = rosterOf(entry(FIRST_CODE, "ALVARADO QUISPE, MARIA FERNANDA"))

        val preview: SiagieImportPreview = previewImport(sectionId, URI)
        applyImport(sectionId, URI, emptySet(), withdrawalDate)

        assertThat((preview as SiagieImportPreview.Ready).plan.updated.map { it.fullName })
            .containsExactly("ALVARADO QUISPE, MARIA FERNANDA")
        assertThat(students.listBySection(sectionId).single().fullName)
            .isEqualTo("ALVARADO QUISPE, MARIA FERNANDA")
    }

    @Test
    fun `a student missing from the template is proposed as withdrawn and never deleted`() = runTest {
        givenSection()
        reader.roster = rosterOf(
            entry(FIRST_CODE, "ALVARADO QUISPE, MARIA"),
            entry(SECOND_CODE, "BAUTISTA HUAMAN, JOSE"),
        )
        applyImport(sectionId, URI, emptySet(), withdrawalDate)
        reader.roster = rosterOf(entry(FIRST_CODE, "ALVARADO QUISPE, MARIA"))

        val preview: SiagieImportPreview = previewImport(sectionId, URI)
        val missing: SiagieImportMissing = (preview as SiagieImportPreview.Ready).plan.missing.single()
        val result: SiagieImportResult = applyImport(sectionId, URI, setOf(missing.studentId), withdrawalDate)

        assertThat(missing.fullName).isEqualTo("BAUTISTA HUAMAN, JOSE")
        assertThat(result).isEqualTo(SiagieImportResult.Applied(created = 0, updated = 0, withdrawn = 1))
        val withdrawn: Student = students.listBySection(sectionId).single { it.code.value == SECOND_CODE }
        assertThat(withdrawn.withdrawalDate).isEqualTo(withdrawalDate)
    }

    @Test
    fun `a missing student left unselected keeps taking part in the section`() = runTest {
        givenSection()
        reader.roster = rosterOf(
            entry(FIRST_CODE, "ALVARADO QUISPE, MARIA"),
            entry(SECOND_CODE, "BAUTISTA HUAMAN, JOSE"),
        )
        applyImport(sectionId, URI, emptySet(), withdrawalDate)
        reader.roster = rosterOf(entry(FIRST_CODE, "ALVARADO QUISPE, MARIA"))

        applyImport(sectionId, URI, emptySet(), withdrawalDate)

        val kept: Student = students.listBySection(sectionId).single { it.code.value == SECOND_CODE }
        assertThat(kept.withdrawalDate).isNull()
    }

    @Test
    fun `a student that comes back in the template is reactivated`() = runTest {
        givenSection()
        reader.roster = rosterOf(
            entry(FIRST_CODE, "ALVARADO QUISPE, MARIA"),
            entry(SECOND_CODE, "BAUTISTA HUAMAN, JOSE"),
        )
        applyImport(sectionId, URI, emptySet(), withdrawalDate)
        reader.roster = rosterOf(entry(FIRST_CODE, "ALVARADO QUISPE, MARIA"))
        val missing: StudentId = plannedMissing()
        applyImport(sectionId, URI, setOf(missing), withdrawalDate)
        reader.roster = rosterOf(
            entry(FIRST_CODE, "ALVARADO QUISPE, MARIA"),
            entry(SECOND_CODE, "BAUTISTA HUAMAN, JOSE"),
        )

        applyImport(sectionId, URI, emptySet(), withdrawalDate)

        val returning: Student = students.listBySection(sectionId).single { it.code.value == SECOND_CODE }
        assertThat(returning.withdrawalDate).isNull()
        assertThat(returning.id).isEqualTo(missing)
    }

    @Test
    fun `a template of another grade is rejected with both grades`() = runTest {
        givenSection(Grade.THIRD)
        reader.roster = SiagieRoster(
            gradeNumber = 6,
            sectionName = null,
            students = listOf(entry(FIRST_CODE, "ALVARADO QUISPE, MARIA")),
        )

        val preview: SiagieImportPreview = previewImport(sectionId, URI)

        assertThat(preview).isEqualTo(
            SiagieImportPreview.Rejected(SiagieImportRejection.GradeMismatch(expected = 3, found = 6), FILE_NAME)
        )
    }

    @Test
    fun `a template of another section is rejected with both sections`() = runTest {
        givenSection()
        reader.roster = SiagieRoster(
            gradeNumber = 6,
            sectionName = "B",
            students = listOf(entry(FIRST_CODE, "ALVARADO QUISPE, MARIA")),
        )

        val preview: SiagieImportPreview = previewImport(sectionId, URI)

        assertThat(preview).isEqualTo(
            SiagieImportPreview.Rejected(SiagieImportRejection.SectionMismatch(expected = "A", found = "B"), FILE_NAME)
        )
    }

    @Test
    fun `a file that is not a siagie template is rejected`() = runTest {
        givenSection()
        reader.roster = null

        val preview: SiagieImportPreview = previewImport(sectionId, URI)

        assertThat(preview).isEqualTo(SiagieImportPreview.Rejected(SiagieImportRejection.NotASiagieTemplate, FILE_NAME))
    }

    @Test
    fun `a template without students is rejected`() = runTest {
        givenSection()
        reader.roster = SiagieRoster(gradeNumber = 6, sectionName = null, students = emptyList())

        val preview: SiagieImportPreview = previewImport(sectionId, URI)

        assertThat(preview).isEqualTo(SiagieImportPreview.Rejected(SiagieImportRejection.EmptyRoster, FILE_NAME))
    }

    @Test
    fun `a rejected template writes nothing`() = runTest {
        givenSection(Grade.THIRD)
        reader.roster = SiagieRoster(
            gradeNumber = 6,
            sectionName = null,
            students = listOf(entry(FIRST_CODE, "ALVARADO QUISPE, MARIA")),
        )

        val result: SiagieImportResult = applyImport(sectionId, URI, emptySet(), withdrawalDate)

        assertThat(result).isEqualTo(
            SiagieImportResult.Rejected(SiagieImportRejection.GradeMismatch(expected = 3, found = 6))
        )
        assertThat(students.listBySection(sectionId)).isEmpty()
        assertThat(store.findTemplate(sectionId, ImportedTemplateKind.GRADES)).isNull()
    }

    private suspend fun plannedMissing(): StudentId {
        val preview: SiagieImportPreview = previewImport(sectionId, URI)
        return (preview as SiagieImportPreview.Ready).plan.missing.single().studentId
    }

    private suspend fun givenSection(grade: Grade = Grade.SIXTH) {
        sections.save(Section(id = sectionId, schoolYearId = SchoolYearId("year-1"), grade = grade, name = "A"))
    }

    private fun rosterOf(vararg students: SiagieRosterStudent): SiagieRoster =
        SiagieRoster(gradeNumber = 6, sectionName = null, students = students.toList())

    private fun entry(code: String, fullName: String): SiagieRosterStudent = SiagieRosterStudent(
        siagieId = if (code == FIRST_CODE) "1001" else "1002",
        code = StudentCode(code),
        fullName = fullName,
    )
}

private val content: ByteArray = byteArrayOf(1, 2, 3, 4)

private class FakeSiagieDocuments(
    private val fileName: String,
    private val content: ByteArray,
) : SiagieDocuments {

    override suspend fun nameOf(uri: String): String = fileName

    override suspend fun readContent(uri: String): ByteArray = content
}

private class FakeSiagieRosterReader : SiagieRosterReader {

    var roster: SiagieRoster? = null

    override fun read(fileName: String, content: ByteArray): SiagieRosterResult {
        val parsed: SiagieRoster = roster ?: return SiagieRosterResult.NotASiagieTemplate
        return SiagieRosterResult.Parsed(parsed)
    }
}
