package com.emm.gema.core.database.evaluation

import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.inMemoryGemaDb
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.PeriodLevelRepository
import com.emm.gema.core.domain.evaluation.UnworkedComment
import com.emm.gema.core.domain.section.Area
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

private const val SECTION_ID: String = "section-1"
private const val PERIOD_ID: String = "period-1"

class PeriodLevelPersistenceTest {

    private val database: GemaDb = inMemoryGemaDb()
    private val repository: PeriodLevelRepository =
        SqlDelightPeriodLevelRepository(database, UnconfinedTestDispatcher())

    @Test
    fun `a saved level comes back with its conclusion`() = runTest {
        repository.save(
            PeriodLevel(keyOf("student-1", firstCompetency))
                .withAchievementLevel(AchievementLevel.C)
                .withDescriptiveConclusion("Necesita apoyo en la lectura"),
        )

        val stored: PeriodLevel? = repository.find(keyOf("student-1", firstCompetency))

        assertThat(stored?.achievementLevel).isEqualTo(AchievementLevel.C)
        assertThat(stored?.descriptiveConclusion).isEqualTo("Necesita apoyo en la lectura")
        assertThat(stored?.isIncomplete).isFalse()
    }

    @Test
    fun `an unworked comment round trips`() = runTest {
        repository.save(
            PeriodLevel(keyOf("student-1", firstCompetency))
                .withUnworkedComment(UnworkedComment.NOT_ENOUGH_EVIDENCE),
        )

        val stored: PeriodLevel? = repository.find(keyOf("student-1", firstCompetency))

        assertThat(stored?.unworkedComment).isEqualTo(UnworkedComment.NOT_ENOUGH_EVIDENCE)
        assertThat(stored?.achievementLevel).isNull()
    }

    @Test
    fun `saving the same cell twice leaves one row`() = runTest {
        repository.save(PeriodLevel(keyOf("student-1", firstCompetency)).withAchievementLevel(AchievementLevel.A))
        repository.save(PeriodLevel(keyOf("student-1", firstCompetency)).withAchievementLevel(AchievementLevel.B))

        val stored: List<PeriodLevel> = repository.observeByPeriod(SECTION_ID, PERIOD_ID).first()

        assertThat(stored).hasSize(1)
        assertThat(stored.single().achievementLevel).isEqualTo(AchievementLevel.B)
    }

    @Test
    fun `deleting a cell removes only that cell`() = runTest {
        repository.save(PeriodLevel(keyOf("student-1", firstCompetency)).withAchievementLevel(AchievementLevel.A))
        repository.save(PeriodLevel(keyOf("student-2", firstCompetency)).withAchievementLevel(AchievementLevel.A))

        repository.delete(keyOf("student-1", firstCompetency))

        val stored: List<PeriodLevel> = repository.observeByPeriod(SECTION_ID, PERIOD_ID).first()
        assertThat(stored.map { it.key.studentId }).containsExactly("student-2")
    }

    @Test
    fun `recorded counts skip a cell that only holds a conclusion`() = runTest {
        repository.save(PeriodLevel(keyOf("student-1", firstCompetency)).withAchievementLevel(AchievementLevel.A))
        repository.save(PeriodLevel(keyOf("student-2", firstCompetency)).withDescriptiveConclusion("Pendiente"))

        val counts: Map<String, Int> = repository.observeRecordedCountsByPeriod(SECTION_ID, PERIOD_ID).first()

        assertThat(counts[firstCompetency]).isEqualTo(1)
    }

    @Test
    fun `section counts add every period`() = runTest {
        repository.save(PeriodLevel(keyOf("student-1", firstCompetency)).withAchievementLevel(AchievementLevel.A))
        repository.save(
            PeriodLevel(
                PeriodLevelKey(
                    sectionId = SECTION_ID,
                    periodId = "period-2",
                    studentId = "student-1",
                    competencyId = firstCompetency,
                ),
            ).withAchievementLevel(AchievementLevel.B),
        )

        val counts: Map<String, Int> = repository.observeRecordedCountsBySection(SECTION_ID).first()

        assertThat(counts[firstCompetency]).isEqualTo(2)
    }

    @Test
    fun `clearing a section removes its levels`() = runTest {
        repository.save(PeriodLevel(keyOf("student-1", firstCompetency)).withAchievementLevel(AchievementLevel.A))

        repository.clearSection(SECTION_ID)

        assertThat(repository.observeByPeriod(SECTION_ID, PERIOD_ID).first()).isEmpty()
    }

    private fun keyOf(studentId: String, competencyId: String): PeriodLevelKey = PeriodLevelKey(
        sectionId = SECTION_ID,
        periodId = PERIOD_ID,
        studentId = studentId,
        competencyId = competencyId,
    )

    private val firstCompetency: String = Competency.idOf(Area.PPSS, 1)
}
