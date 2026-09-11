package com.emm.gema.core.domain.activity

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.SavePeriodLevelUseCase
import com.emm.gema.core.domain.fake.InMemoryActivityRepository
import com.emm.gema.core.domain.fake.InMemoryEvidenceLevelRepository
import com.emm.gema.core.domain.fake.InMemoryPeriodLevelRepository
import com.emm.gema.core.domain.fake.InMemoryPeriodRepository
import com.emm.gema.core.domain.fake.InMemorySectionRepository
import com.emm.gema.core.domain.fake.SequentialIdGenerator
import com.emm.gema.core.domain.schoolyear.FindPeriodForDateUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

private val sectionId: SectionId = SectionId("section-1")
private val schoolYearId: SchoolYearId = SchoolYearId("year-1")
private val firstPeriodId: PeriodId = PeriodId("period-1")
private val secondPeriodId: PeriodId = PeriodId("period-2")

class ActivityUseCasesTest {

    private val sectionRepository: InMemorySectionRepository = InMemorySectionRepository()
    private val periodRepository: InMemoryPeriodRepository = InMemoryPeriodRepository()
    private val activityRepository: InMemoryActivityRepository = InMemoryActivityRepository()
    private val evidenceLevelRepository: InMemoryEvidenceLevelRepository =
        InMemoryEvidenceLevelRepository(activityRepository)
    private val periodLevelRepository: InMemoryPeriodLevelRepository = InMemoryPeriodLevelRepository()

    private val findPeriodForDate: FindPeriodForDateUseCase = FindPeriodForDateUseCase(periodRepository)
    private val saveActivity: SaveActivityUseCase = SaveActivityUseCase(
        sectionRepository = sectionRepository,
        findPeriodForDate = findPeriodForDate,
        activityRepository = activityRepository,
        idGenerator = SequentialIdGenerator("activity"),
    )
    private val deleteActivity: DeleteActivityUseCase =
        DeleteActivityUseCase(activityRepository, evidenceLevelRepository)
    private val getActivities: GetActivitiesUseCase = GetActivitiesUseCase(activityRepository)
    private val recordEvidenceLevel: RecordEvidenceLevelUseCase = RecordEvidenceLevelUseCase(evidenceLevelRepository)
    private val getEvidenceForActivity: GetEvidenceForActivityUseCase =
        GetEvidenceForActivityUseCase(evidenceLevelRepository)
    private val getEvidenceStudentCounts: GetActivityEvidenceStudentCountsUseCase =
        GetActivityEvidenceStudentCountsUseCase(evidenceLevelRepository)
    private val getEvidenceForPeriodLevel: GetEvidenceForPeriodLevelUseCase =
        GetEvidenceForPeriodLevelUseCase(evidenceLevelRepository)
    private val savePeriodLevel: SavePeriodLevelUseCase = SavePeriodLevelUseCase(periodLevelRepository)

    @Before
    fun setUp() = runTest {
        sectionRepository.save(Section(sectionId, schoolYearId, Grade.THIRD, "3ro A"))
        periodRepository.saveAll(
            listOf(
                Period(firstPeriodId, schoolYearId, 1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 15)),
                Period(secondPeriodId, schoolYearId, 2, LocalDate.of(2026, 5, 16), LocalDate.of(2026, 7, 31)),
            ),
        )
    }

    @Test
    fun `creating an activity derives its period from the date`() = runTest {
        val activity: Activity = saveActivityOk(
            sectionId = sectionId,
            activityId = null,
            name = "Debate del aula",
            date = LocalDate.of(2026, 3, 10),
            competencyIds = setOf(firstPpssId),
        )

        assertThat(activity.periodId).isEqualTo(firstPeriodId)
        assertThat(getActivities(sectionId, firstPeriodId).first()).containsExactly(activity)
    }

    @Test
    fun `changing the date moves the activity to another period`() = runTest {
        val created: Activity = saveActivityOk(
            sectionId = sectionId,
            activityId = null,
            name = "Debate del aula",
            date = LocalDate.of(2026, 3, 10),
            competencyIds = setOf(firstPpssId),
        )

        val moved: Activity = saveActivityOk(
            sectionId = sectionId,
            activityId = created.id,
            name = created.name,
            date = LocalDate.of(2026, 6, 1),
            competencyIds = created.competencyIds,
        )

        assertThat(moved.periodId).isEqualTo(secondPeriodId)
        assertThat(getActivities(sectionId, firstPeriodId).first()).isEmpty()
        assertThat(getActivities(sectionId, secondPeriodId).first()).containsExactly(moved)
    }

    @Test
    fun `a date outside every period is reported, not thrown`() = runTest {
        val result: SaveActivityResult = saveActivity(
            sectionId = sectionId,
            activityId = null,
            name = "Debate del aula",
            date = LocalDate.of(2027, 1, 1),
            competencyIds = setOf(firstPpssId),
        )

        assertThat(result).isEqualTo(SaveActivityResult.DateOutsidePeriods)
        assertThat(getActivities(sectionId, firstPeriodId).first()).isEmpty()
    }

    @Test
    fun `recording an evidence level stores it`() = runTest {
        val activity: Activity = createActivity()
        val key = EvidenceLevelKey(activity.id, firstStudentId, firstPpssId)

        recordEvidenceLevel(key, AchievementLevel.B)

        val stored: List<EvidenceLevel> = getEvidenceForActivity(activity.id).first()
        assertThat(stored).containsExactly(EvidenceLevel(key, AchievementLevel.B))
    }

    @Test
    fun `evidence levels are optional per student and competency`() = runTest {
        val activity: Activity = createActivity()
        recordEvidenceLevel(EvidenceLevelKey(activity.id, firstStudentId, firstPpssId), AchievementLevel.A)

        val stored: List<EvidenceLevel> = getEvidenceForActivity(activity.id).first()

        assertThat(stored.map { it.key.studentId }).containsExactly(firstStudentId)
    }

    @Test
    fun `recording no level clears a previously recorded one`() = runTest {
        val activity: Activity = createActivity()
        val key = EvidenceLevelKey(activity.id, firstStudentId, firstPpssId)
        recordEvidenceLevel(key, AchievementLevel.A)

        recordEvidenceLevel(key, null)

        assertThat(getEvidenceForActivity(activity.id).first()).isEmpty()
    }

    @Test
    fun `deleting an activity deletes its evidence levels`() = runTest {
        val activity: Activity = createActivity()
        recordEvidenceLevel(EvidenceLevelKey(activity.id, firstStudentId, firstPpssId), AchievementLevel.A)

        deleteActivity(activity.id)

        assertThat(getEvidenceForActivity(activity.id).first()).isEmpty()
        assertThat(getActivities(sectionId, activity.periodId).first()).isEmpty()
    }

    @Test
    fun `editing an activity never touches period levels`() = runTest {
        val activity: Activity = createActivity()
        val periodLevelKey = PeriodLevelKey(sectionId, activity.periodId, firstStudentId, firstPpssId)
        savePeriodLevel(PeriodLevel(periodLevelKey).withAchievementLevel(AchievementLevel.B))

        saveActivityOk(
            sectionId = sectionId,
            activityId = activity.id,
            name = "Nuevo nombre",
            date = activity.date,
            competencyIds = activity.competencyIds,
        )

        assertThat(periodLevelRepository.observeByPeriod(sectionId, activity.periodId).first())
            .containsExactly(PeriodLevel(periodLevelKey).withAchievementLevel(AchievementLevel.B))
    }

    @Test
    fun `deleting an activity never touches period levels`() = runTest {
        val activity: Activity = createActivity()
        val periodLevelKey = PeriodLevelKey(sectionId, activity.periodId, firstStudentId, firstPpssId)
        savePeriodLevel(PeriodLevel(periodLevelKey).withAchievementLevel(AchievementLevel.B))

        deleteActivity(activity.id)

        assertThat(periodLevelRepository.observeByPeriod(sectionId, activity.periodId).first())
            .containsExactly(PeriodLevel(periodLevelKey).withAchievementLevel(AchievementLevel.B))
    }

    @Test
    fun `the evidence count is the number of distinct students recorded`() = runTest {
        val activity: Activity = createActivity()
        recordEvidenceLevel(EvidenceLevelKey(activity.id, firstStudentId, firstPpssId), AchievementLevel.A)
        recordEvidenceLevel(EvidenceLevelKey(activity.id, secondStudentId, firstPpssId), AchievementLevel.B)

        val counts: Map<ActivityId, Int> = getEvidenceStudentCounts(sectionId, activity.periodId).first()

        assertThat(counts[activity.id]).isEqualTo(2)
    }

    @Test
    fun `evidence for a period level is ordered by activity date`() = runTest {
        val first: Activity = createActivity()
        val second: Activity = saveActivityOk(
            sectionId = sectionId,
            activityId = null,
            name = "Ficha de convivencia",
            date = LocalDate.of(2026, 3, 20),
            competencyIds = setOf(firstPpssId),
        )
        recordEvidenceLevel(EvidenceLevelKey(second.id, firstStudentId, firstPpssId), AchievementLevel.B)
        recordEvidenceLevel(EvidenceLevelKey(first.id, firstStudentId, firstPpssId), AchievementLevel.A)

        val evidence: List<EvidenceRecord> =
            getEvidenceForPeriodLevel(sectionId, first.periodId, firstStudentId, firstPpssId).first()

        assertThat(evidence.map { it.activityId }).containsExactly(first.id, second.id).inOrder()
    }

    private suspend fun createActivity(): Activity = saveActivityOk(
        sectionId = sectionId,
        activityId = null,
        name = "Debate del aula",
        date = LocalDate.of(2026, 3, 10),
        competencyIds = setOf(firstPpssId),
    )

    private suspend fun saveActivityOk(
        sectionId: SectionId,
        activityId: ActivityId?,
        name: String,
        date: LocalDate,
        competencyIds: Set<CompetencyId>,
    ): Activity {
        val result: SaveActivityResult = saveActivity(sectionId, activityId, name, date, competencyIds)
        return (result as SaveActivityResult.Saved).activity
    }
}

private val firstStudentId: StudentId = StudentId("student-1")

private val secondStudentId: StudentId = StudentId("student-2")

private val firstPpssId: CompetencyId = CompetencyId("PPSS-1")
