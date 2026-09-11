package com.emm.gema.core.database.curriculum

import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.inMemoryGemaDb
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyRepository
import com.emm.gema.core.domain.curriculum.PrimaryCurriculum
import com.emm.gema.core.domain.curriculum.SeedCurriculumUseCase
import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import com.emm.gema.core.domain.section.Area
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

private const val SECTION_ID: String = "section-1"
private const val PERIOD_ID: String = "period-1"

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
        val target: String = Competency.idOf(Area.PPSS, 3)

        workedCompetencyRepository.setWorked(SECTION_ID, PERIOD_ID, target, isWorked = true)

        assertThat(workedCompetencyRepository.observeWorked(SECTION_ID, PERIOD_ID).first()).containsExactly(target)
    }

    @Test
    fun `marking the same competency twice writes one row`() = runTest {
        seedCurriculum()
        val target: String = Competency.idOf(Area.PPSS, 3)

        workedCompetencyRepository.setWorked(SECTION_ID, PERIOD_ID, target, isWorked = true)
        workedCompetencyRepository.setWorked(SECTION_ID, PERIOD_ID, target, isWorked = true)

        assertThat(workedCompetencyRepository.observeWorked(SECTION_ID, PERIOD_ID).first()).hasSize(1)
    }

    @Test
    fun `unmarking a competency removes only that row`() = runTest {
        seedCurriculum()
        val kept: String = Competency.idOf(Area.PPSS, 1)
        val dropped: String = Competency.idOf(Area.PPSS, 2)
        workedCompetencyRepository.setWorked(SECTION_ID, PERIOD_ID, kept, isWorked = true)
        workedCompetencyRepository.setWorked(SECTION_ID, PERIOD_ID, dropped, isWorked = true)

        workedCompetencyRepository.setWorked(SECTION_ID, PERIOD_ID, dropped, isWorked = false)

        assertThat(workedCompetencyRepository.observeWorked(SECTION_ID, PERIOD_ID).first()).containsExactly(kept)
    }

    @Test
    fun `clearing a section leaves the other sections alone`() = runTest {
        seedCurriculum()
        val target: String = Competency.idOf(Area.MATE, 1)
        workedCompetencyRepository.setWorked(SECTION_ID, PERIOD_ID, target, isWorked = true)
        workedCompetencyRepository.setWorked("section-2", PERIOD_ID, target, isWorked = true)

        workedCompetencyRepository.clearSection(SECTION_ID)

        assertThat(workedCompetencyRepository.observeWorked(SECTION_ID, PERIOD_ID).first()).isEmpty()
        assertThat(workedCompetencyRepository.observeWorked("section-2", PERIOD_ID).first()).containsExactly(target)
    }
}
