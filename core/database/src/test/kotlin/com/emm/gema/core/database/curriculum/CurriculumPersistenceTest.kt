package com.emm.gema.core.database.curriculum

import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.inMemoryGemaDb
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.curriculum.CompetencyRepository
import com.emm.gema.core.domain.curriculum.PrimaryCurriculum
import com.emm.gema.core.domain.curriculum.SeedCurriculumUseCase
import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.SectionId
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

private val sectionId: SectionId = SectionId("section-1")
private val periodId: PeriodId = PeriodId("period-1")

class CurriculumPersistenceTest {

    private val database: GemaDb = inMemoryGemaDb()
    private val competencyRepository: CompetencyRepository =
        SqlDelightCompetencyRepository(database, UnconfinedTestDispatcher())
    private val workedCompetencyRepository: WorkedCompetencyRepository =
        SqlDelightWorkedCompetencyRepository(database, UnconfinedTestDispatcher())
    private val seedCurriculum: SeedCurriculumUseCase = SeedCurriculumUseCase(competencyRepository)

    @Test
    fun `seeding writes every competency with its area and ordinal`() = runTest {
        seedCurriculum()

        val stored: List<Competency> = competencyRepository.findByArea(Area.MATE)
        assertThat(stored.map { it.siagieOrdinal }).containsExactly(1, 2, 3, 4).inOrder()
        assertThat(stored.first().name).isEqualTo("Resuelve problemas de cantidad")
    }

    @Test
    fun `seeding twice leaves one row per competency`() = runTest {
        seedCurriculum()
        seedCurriculum()

        val rows: List<String> = database.competencyQueries.selectAll().executeAsList().map { it.id }
        assertThat(rows).hasSize(PrimaryCurriculum.competencies.size)
        assertThat(rows).containsNoDuplicates()
    }

    @Test
    fun `seeding stores the curriculum version of the seed`() = runTest {
        seedCurriculum()

        val versions: Set<Long> = database.competencyQueries.selectAll()
            .executeAsList()
            .mapTo(mutableSetOf()) { it.curriculum_version }
        assertThat(versions).containsExactly(PrimaryCurriculum.VERSION.toLong())
    }

    @Test
    fun `a marked competency survives a read back`() = runTest {
        seedCurriculum()
        val target: CompetencyId = Competency.idOf(Area.PPSS, 3)

        workedCompetencyRepository.setWorked(sectionId, periodId, target, isWorked = true)

        assertThat(workedCompetencyRepository.observeWorked(sectionId, periodId).first()).containsExactly(target)
    }

    @Test
    fun `marking the same competency twice writes one row`() = runTest {
        seedCurriculum()
        val target: CompetencyId = Competency.idOf(Area.PPSS, 3)

        workedCompetencyRepository.setWorked(sectionId, periodId, target, isWorked = true)
        workedCompetencyRepository.setWorked(sectionId, periodId, target, isWorked = true)

        assertThat(workedCompetencyRepository.observeWorked(sectionId, periodId).first()).hasSize(1)
    }

    @Test
    fun `unmarking a competency removes only that row`() = runTest {
        seedCurriculum()
        val kept: CompetencyId = Competency.idOf(Area.PPSS, 1)
        val dropped: CompetencyId = Competency.idOf(Area.PPSS, 2)
        workedCompetencyRepository.setWorked(sectionId, periodId, kept, isWorked = true)
        workedCompetencyRepository.setWorked(sectionId, periodId, dropped, isWorked = true)

        workedCompetencyRepository.setWorked(sectionId, periodId, dropped, isWorked = false)

        assertThat(workedCompetencyRepository.observeWorked(sectionId, periodId).first()).containsExactly(kept)
    }

    @Test
    fun `clearing a section leaves the other sections alone`() = runTest {
        seedCurriculum()
        val target: CompetencyId = Competency.idOf(Area.MATE, 1)
        workedCompetencyRepository.setWorked(sectionId, periodId, target, isWorked = true)
        workedCompetencyRepository.setWorked(
            SectionId("section-2"),
            periodId,
            target,
            isWorked = true,
        )

        workedCompetencyRepository.clearSection(sectionId)

        assertThat(workedCompetencyRepository.observeWorked(sectionId, periodId).first()).isEmpty()
        val otherSectionId = SectionId("section-2")
        val other: Set<CompetencyId> = workedCompetencyRepository.observeWorked(otherSectionId, periodId).first()
        assertThat(other).containsExactly(target)
    }
}
