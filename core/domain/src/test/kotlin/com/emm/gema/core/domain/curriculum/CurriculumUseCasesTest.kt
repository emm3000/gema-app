package com.emm.gema.core.domain.curriculum

import com.emm.gema.core.domain.fake.InMemoryCompetencyRepository
import com.emm.gema.core.domain.fake.InMemorySectionAreaRepository
import com.emm.gema.core.domain.fake.InMemoryWorkedCompetencyRepository
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.SectionId
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

private val sectionId: SectionId = SectionId("section-1")
private val periodId: PeriodId = PeriodId("period-1")

class CurriculumUseCasesTest {

    private val competencyRepository: InMemoryCompetencyRepository = InMemoryCompetencyRepository()
    private val workedCompetencyRepository: InMemoryWorkedCompetencyRepository = InMemoryWorkedCompetencyRepository()
    private val sectionAreaRepository: InMemorySectionAreaRepository = InMemorySectionAreaRepository()
    private val seedCurriculum: SeedCurriculumUseCase = SeedCurriculumUseCase(competencyRepository)
    private val getPeriodCompetencies: GetPeriodCompetenciesUseCase = GetPeriodCompetenciesUseCase(
        competencyRepository = competencyRepository,
        workedCompetencyRepository = workedCompetencyRepository,
    )
    private val setCompetencyWorked: SetCompetencyWorkedUseCase =
        SetCompetencyWorkedUseCase(workedCompetencyRepository)
    private val getWorkedCompetencies: GetWorkedCompetenciesUseCase = GetWorkedCompetenciesUseCase(
        competencyRepository = competencyRepository,
        workedCompetencyRepository = workedCompetencyRepository,
        sectionAreaRepository = sectionAreaRepository,
    )

    @Test
    fun `seeding writes the whole primary curriculum`() = runTest {
        seedCurriculum()

        assertThat(competencyRepository.all()).hasSize(PrimaryCurriculum.competencies.size)
        assertThat(competencyRepository.seededVersion).isEqualTo(PrimaryCurriculum.VERSION)
    }

    @Test
    fun `seeding twice never duplicates a competency`() = runTest {
        seedCurriculum()
        seedCurriculum()

        assertThat(competencyRepository.seedCallCount).isEqualTo(2)
        assertThat(competencyRepository.all()).hasSize(PrimaryCurriculum.competencies.size)
        assertThat(competencyRepository.all().map { it.id }).containsNoDuplicates()
    }

    @Test
    fun `an area starts with no worked competency`() = runTest {
        seedCurriculum()

        val competencies: List<PeriodCompetency> = getPeriodCompetencies(sectionId, periodId, Area.PPSS).first()

        assertThat(competencies).hasSize(5)
        assertThat(competencies.none { it.isWorked }).isTrue()
    }

    @Test
    fun `marking a competency makes it worked for that section and period`() = runTest {
        seedCurriculum()
        val target: CompetencyId = Competency.idOf(Area.PPSS, 2)

        setCompetencyWorked(sectionId, periodId, target, isWorked = true)

        val worked: List<CompetencyId> = getPeriodCompetencies(sectionId, periodId, Area.PPSS).first()
            .filter { it.isWorked }
            .map { it.competency.id }
        assertThat(worked).containsExactly(target)
    }

    @Test
    fun `unmarking a competency drops it from the worked ones`() = runTest {
        seedCurriculum()
        val target: CompetencyId = Competency.idOf(Area.PPSS, 2)

        setCompetencyWorked(sectionId, periodId, target, isWorked = true)
        setCompetencyWorked(sectionId, periodId, target, isWorked = false)

        val competencies: List<PeriodCompetency> = getPeriodCompetencies(sectionId, periodId, Area.PPSS).first()
        assertThat(competencies.none { it.isWorked }).isTrue()
    }

    @Test
    fun `a worked competency belongs to one period only`() = runTest {
        seedCurriculum()
        val target: CompetencyId = Competency.idOf(Area.MATE, 1)

        setCompetencyWorked(sectionId, periodId, target, isWorked = true)

        val other: List<PeriodCompetency> = getPeriodCompetencies(sectionId, PeriodId("period-2"), Area.MATE).first()
        assertThat(other.none { it.isWorked }).isTrue()
    }

    @Test
    fun `deleting a section clears its worked competencies`() = runTest {
        seedCurriculum()
        setCompetencyWorked(sectionId, periodId, Competency.idOf(Area.MATE, 1), isWorked = true)
        setCompetencyWorked(SectionId("section-2"), periodId, Competency.idOf(Area.MATE, 1), isWorked = true)

        workedCompetencyRepository.clearSection(sectionId)

        assertThat(getPeriodCompetencies(sectionId, periodId, Area.MATE).first().none { it.isWorked }).isTrue()
        val other: List<PeriodCompetency> = getPeriodCompetencies(SectionId("section-2"), periodId, Area.MATE).first()
        assertThat(other.count { it.isWorked }).isEqualTo(1)
    }

    @Test
    fun `worked competencies span every area`() = runTest {
        seedCurriculum()
        setCompetencyWorked(sectionId, periodId, Competency.idOf(Area.PPSS, 1), isWorked = true)
        setCompetencyWorked(sectionId, periodId, Competency.idOf(Area.MATE, 1), isWorked = true)

        val worked: List<Competency> = getWorkedCompetencies(sectionId, periodId).first()

        assertThat(worked.map { it.area }).containsExactly(Area.PPSS, Area.MATE)
    }

    @Test
    fun `a hidden area drops its worked competencies`() = runTest {
        seedCurriculum()
        setCompetencyWorked(sectionId, periodId, Competency.idOf(Area.PPSS, 1), isWorked = true)
        sectionAreaRepository.setAreaHidden(sectionId, Area.PPSS, isHidden = true)

        val worked: List<Competency> = getWorkedCompetencies(sectionId, periodId).first()

        assertThat(worked).isEmpty()
    }
}
