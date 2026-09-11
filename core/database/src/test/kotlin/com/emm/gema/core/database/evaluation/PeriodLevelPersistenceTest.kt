package com.emm.gema.core.database.evaluation

import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.inMemoryGemaDb
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.PeriodLevelRepository
import com.emm.gema.core.domain.evaluation.UnworkedComment
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

private val sectionId: SectionId = SectionId("section-1")
private val periodId: PeriodId = PeriodId("period-1")

class PeriodLevelPersistenceTest {

    private val database: GemaDb = inMemoryGemaDb()
    private val repository: PeriodLevelRepository =
        SqlDelightPeriodLevelRepository(database, UnconfinedTestDispatcher())

    @Test
    fun `a saved level comes back with its conclusion`() = runTest {
        repository.save(
            PeriodLevel(keyOf(firstStudentId, firstCompetency))
                .withAchievementLevel(AchievementLevel.C)
                .withDescriptiveConclusion("Necesita apoyo en la lectura"),
        )

        val stored: PeriodLevel? = repository.find(keyOf(firstStudentId, firstCompetency))

        assertThat(stored?.achievementLevel).isEqualTo(AchievementLevel.C)
        assertThat(stored?.descriptiveConclusion).isEqualTo("Necesita apoyo en la lectura")
        assertThat(stored?.isIncomplete).isFalse()
    }

    @Test
    fun `an unworked comment round trips`() = runTest {
        repository.save(
            PeriodLevel(keyOf(firstStudentId, firstCompetency))
                .withUnworkedComment(UnworkedComment.NOT_ENOUGH_EVIDENCE),
        )

        val stored: PeriodLevel? = repository.find(keyOf(firstStudentId, firstCompetency))

        assertThat(stored?.unworkedComment).isEqualTo(UnworkedComment.NOT_ENOUGH_EVIDENCE)
        assertThat(stored?.achievementLevel).isNull()
    }

    @Test
    fun `saving the same cell twice leaves one row`() = runTest {
        repository.save(PeriodLevel(keyOf(firstStudentId, firstCompetency)).withAchievementLevel(AchievementLevel.A))
        repository.save(PeriodLevel(keyOf(firstStudentId, firstCompetency)).withAchievementLevel(AchievementLevel.B))

        val stored: List<PeriodLevel> = repository.observeByPeriod(sectionId, periodId).first()

        assertThat(stored).hasSize(1)
        assertThat(stored.single().achievementLevel).isEqualTo(AchievementLevel.B)
    }

    @Test
    fun `deleting a cell removes only that cell`() = runTest {
        repository.save(PeriodLevel(keyOf(firstStudentId, firstCompetency)).withAchievementLevel(AchievementLevel.A))
        repository.save(PeriodLevel(keyOf(secondStudentId, firstCompetency)).withAchievementLevel(AchievementLevel.A))

        repository.delete(keyOf(firstStudentId, firstCompetency))

        val stored: List<PeriodLevel> = repository.observeByPeriod(sectionId, periodId).first()
        assertThat(stored.map { it.key.studentId }).containsExactly(secondStudentId)
    }

    @Test
    fun `recorded counts skip a cell that only holds a conclusion`() = runTest {
        repository.save(PeriodLevel(keyOf(firstStudentId, firstCompetency)).withAchievementLevel(AchievementLevel.A))
        repository.save(PeriodLevel(keyOf(secondStudentId, firstCompetency)).withDescriptiveConclusion("Pendiente"))

        val counts: Map<CompetencyId, Int> = repository.observeRecordedCountsByPeriod(sectionId, periodId).first()

        assertThat(counts[firstCompetency]).isEqualTo(1)
    }

    @Test
    fun `section counts add every period`() = runTest {
        repository.save(PeriodLevel(keyOf(firstStudentId, firstCompetency)).withAchievementLevel(AchievementLevel.A))
        repository.save(
            PeriodLevel(
                PeriodLevelKey(
                    sectionId = sectionId,
                    periodId = PeriodId("period-2"),
                    studentId = firstStudentId,
                    competencyId = firstCompetency,
                ),
            ).withAchievementLevel(AchievementLevel.B),
        )

        val counts: Map<CompetencyId, Int> = repository.observeRecordedCountsBySection(sectionId).first()

        assertThat(counts[firstCompetency]).isEqualTo(2)
    }

    @Test
    fun `clearing a section removes its levels`() = runTest {
        repository.save(PeriodLevel(keyOf(firstStudentId, firstCompetency)).withAchievementLevel(AchievementLevel.A))

        repository.clearSection(sectionId)

        assertThat(repository.observeByPeriod(sectionId, periodId).first()).isEmpty()
    }

    private fun keyOf(studentId: StudentId, competencyId: CompetencyId): PeriodLevelKey = PeriodLevelKey(
        sectionId = sectionId,
        periodId = periodId,
        studentId = studentId,
        competencyId = competencyId,
    )

    private val firstCompetency: CompetencyId = Competency.idOf(Area.PPSS, 1)
}

private val firstStudentId: StudentId = StudentId("student-1")

private val secondStudentId: StudentId = StudentId("student-2")
