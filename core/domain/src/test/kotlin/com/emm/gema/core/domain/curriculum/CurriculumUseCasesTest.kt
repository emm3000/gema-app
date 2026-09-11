package com.emm.gema.core.domain.curriculum

import com.emm.gema.core.domain.fake.InMemoryCompetencyRepository
import com.emm.gema.core.domain.fake.InMemoryWorkedCompetencyRepository
import com.emm.gema.core.domain.section.Area
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

private const val SECTION_ID: String = "section-1"
private const val PERIOD_ID: String = "period-1"

class CurriculumUseCasesTest {

    private val competencyRepository: InMemoryCompetencyRepository = InMemoryCompetencyRepository()
    private val workedCompetencyRepository: InMemoryWorkedCompetencyRepository = InMemoryWorkedCompetencyRepository()
    private val seedCurriculum: SeedCurriculumUseCase = SeedCurriculumUseCase(competencyRepository)
    private val getPeriodCompetencies: GetPeriodCompetenciesUseCase = GetPeriodCompetenciesUseCase(
        competencyRepository = competencyRepository,
        workedCompetencyRepository = workedCompetencyRepository,
    )
    private val setCompetencyWorked: SetCompetencyWorkedUseCase =
        SetCompetencyWorkedUseCase(workedCompetencyRepository)

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

        val competencies: List<PeriodCompetency> = getPeriodCompetencies(SECTION_ID, PERIOD_ID, Area.PPSS).first()

        assertThat(competencies).hasSize(5)
        assertThat(competencies.none { it.isWorked }).isTrue()
    }

    @Test
    fun `marking a competency makes it worked for that section and period`() = runTest {
        seedCurriculum()
        val target: String = Competency.idOf(Area.PPSS, 2)

        setCompetencyWorked(SECTION_ID, PERIOD_ID, target, isWorked = true)

        val worked: List<String> = getPeriodCompetencies(SECTION_ID, PERIOD_ID, Area.PPSS).first()
            .filter { it.isWorked }
            .map { it.competency.id }
        assertThat(worked).containsExactly(target)
    }

    @Test
    fun `unmarking a competency drops it from the worked ones`() = runTest {
        seedCurriculum()
        val target: String = Competency.idOf(Area.PPSS, 2)

        setCompetencyWorked(SECTION_ID, PERIOD_ID, target, isWorked = true)
        setCompetencyWorked(SECTION_ID, PERIOD_ID, target, isWorked = false)

        val competencies: List<PeriodCompetency> = getPeriodCompetencies(SECTION_ID, PERIOD_ID, Area.PPSS).first()
        assertThat(competencies.none { it.isWorked }).isTrue()
    }

    @Test
    fun `a worked competency belongs to one period only`() = runTest {
        seedCurriculum()
        val target: String = Competency.idOf(Area.MATE, 1)

        setCompetencyWorked(SECTION_ID, PERIOD_ID, target, isWorked = true)

        val other: List<PeriodCompetency> = getPeriodCompetencies(SECTION_ID, "period-2", Area.MATE).first()
        assertThat(other.none { it.isWorked }).isTrue()
    }

    @Test
    fun `deleting a section clears its worked competencies`() = runTest {
        seedCurriculum()
        setCompetencyWorked(SECTION_ID, PERIOD_ID, Competency.idOf(Area.MATE, 1), isWorked = true)
        setCompetencyWorked("section-2", PERIOD_ID, Competency.idOf(Area.MATE, 1), isWorked = true)

        workedCompetencyRepository.clearSection(SECTION_ID)

        assertThat(getPeriodCompetencies(SECTION_ID, PERIOD_ID, Area.MATE).first().none { it.isWorked }).isTrue()
        assertThat(getPeriodCompetencies("section-2", PERIOD_ID, Area.MATE).first().count { it.isWorked }).isEqualTo(1)
    }
}
