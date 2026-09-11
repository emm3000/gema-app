package com.emm.gema.core.domain.section

import com.emm.gema.core.domain.fake.InMemoryActivityRepository
import com.emm.gema.core.domain.fake.InMemoryAttendanceRepository
import com.emm.gema.core.domain.fake.InMemoryEvidenceLevelRepository
import com.emm.gema.core.domain.fake.InMemoryPeriodLevelRepository
import com.emm.gema.core.domain.fake.InMemorySectionAreaRepository
import com.emm.gema.core.domain.fake.InMemorySectionCascade
import com.emm.gema.core.domain.fake.InMemorySectionRepository
import com.emm.gema.core.domain.fake.InMemorySiagieImportStore
import com.emm.gema.core.domain.fake.InMemoryStudentRepository
import com.emm.gema.core.domain.fake.InMemoryWorkedCompetencyRepository
import com.emm.gema.core.domain.fake.SequentialIdGenerator
import com.emm.gema.core.domain.siagie.ImportedTemplate
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SectionUseCasesTest {

    private val sectionRepository = InMemorySectionRepository()
    private val sectionAreaRepository = InMemorySectionAreaRepository()
    private val workedCompetencyRepository = InMemoryWorkedCompetencyRepository()
    private val createSection = CreateSectionUseCase(sectionRepository, SequentialIdGenerator("section"))
    private val updateSection = UpdateSectionUseCase(sectionRepository)
    private val studentRepository = InMemoryStudentRepository()
    private val siagieImportStore = InMemorySiagieImportStore(studentRepository)
    private val attendanceRepository = InMemoryAttendanceRepository()
    private val activityRepository = InMemoryActivityRepository()
    private val deleteSection = DeleteSectionUseCase(
        InMemorySectionCascade(
            sectionRepository,
            sectionAreaRepository,
            workedCompetencyRepository,
            studentRepository,
            siagieImportStore,
            InMemoryPeriodLevelRepository(),
            attendanceRepository,
            activityRepository,
            InMemoryEvidenceLevelRepository(activityRepository),
        ),
    )
    private val getSections = GetSectionsUseCase(sectionRepository)
    private val getSectionAreas = GetSectionAreasUseCase(sectionAreaRepository)
    private val setAreaVisibility = SetAreaVisibilityUseCase(sectionAreaRepository)

    @Test
    fun `deleting a section drops the siagie template imported for it`() = runTest {
        val section: Section = createSection("2026", Grade.THIRD, "A").let {
            getSections("2026").first().single()
        }
        siagieImportStore.apply(
            emptyList(),
            ImportedTemplate(
                sectionId = section.id,
                kind = ImportedTemplateKind.GRADES,
                fileName = "3 Primaria EBR.xlsx",
                content = byteArrayOf(1, 2, 3),
                importedAt = Instant.parse("2026-09-10T12:00:00Z"),
            ),
        )

        deleteSection(section.id)

        assertThat(siagieImportStore.findTemplate(section.id, ImportedTemplateKind.GRADES)).isNull()
    }

    @Test
    fun `a created section is listed under its school year`() = runTest {
        createSection(schoolYearId = "2026", grade = Grade.THIRD, name = "A")

        val sections: List<Section> = getSections("2026").first()

        assertThat(sections.single().grade).isEqualTo(Grade.THIRD)
        assertThat(sections.single().name).isEqualTo("A")
        assertThat(getSections("2025").first()).isEmpty()
    }

    @Test
    fun `sections are listed by grade and then by name`() = runTest {
        createSection("2026", Grade.SECOND, "B")
        createSection("2026", Grade.FIRST, "B")
        createSection("2026", Grade.SECOND, "A")

        val sections: List<Section> = getSections("2026").first()

        assertThat(sections.map { "${it.grade.number}${it.name}" })
            .containsExactly("1B", "2A", "2B")
            .inOrder()
    }

    @Test
    fun `a renamed section keeps its id`() = runTest {
        val created: Section = createSection("2026", Grade.FIRST, "A")

        updateSection(sectionId = created.id, grade = Grade.FOURTH, name = "Unica")

        val stored: Section = getSections("2026").first().single()
        assertThat(stored.id).isEqualTo(created.id)
        assertThat(stored.grade).isEqualTo(Grade.FOURTH)
        assertThat(stored.name).isEqualTo("Unica")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `updating a section that does not exist is rejected`() = runTest {
        updateSection(sectionId = "missing", grade = Grade.FIRST, name = "A")
    }

    @Test
    fun `a deleted section disappears with its area visibility`() = runTest {
        val created: Section = createSection("2026", Grade.FIRST, "A")
        setAreaVisibility(created.id, Area.EFIS, isActive = false)

        deleteSection(created.id)

        assertThat(getSections("2026").first()).isEmpty()
        assertThat(getSectionAreas(created.id).first().filter { it.isActive }).hasSize(Area.entries.size)
    }

    @Test
    fun `a new section starts with every area active`() = runTest {
        val created: Section = createSection("2026", Grade.FIRST, "A")

        val areas: List<SectionArea> = getSectionAreas(created.id).first()

        assertThat(areas.map { it.area }).containsExactlyElementsIn(Area.entries).inOrder()
        assertThat(areas.all { it.isActive }).isTrue()
    }

    @Test
    fun `hiding and unhiding an area is remembered`() = runTest {
        val created: Section = createSection("2026", Grade.FIRST, "A")

        setAreaVisibility(created.id, Area.EREL, isActive = false)
        assertThat(getSectionAreas(created.id).first().single { it.area == Area.EREL }.isActive).isFalse()

        setAreaVisibility(created.id, Area.EREL, isActive = true)
        assertThat(getSectionAreas(created.id).first().single { it.area == Area.EREL }.isActive).isTrue()
    }

    @Test
    fun `hiding an area of one section leaves the other sections untouched`() = runTest {
        val first: Section = createSection("2026", Grade.FIRST, "A")
        val second: Section = createSection("2026", Grade.SECOND, "B")

        setAreaVisibility(first.id, Area.ARTE, isActive = false)

        assertThat(getSectionAreas(second.id).first().all { it.isActive }).isTrue()
    }
}
