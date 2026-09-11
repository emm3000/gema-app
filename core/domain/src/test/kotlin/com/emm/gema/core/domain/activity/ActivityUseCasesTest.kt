package com.emm.gema.core.domain.activity

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
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

private const val SECTION_ID: String = "section-1"
private const val SCHOOL_YEAR_ID: String = "year-1"
private const val FIRST_PERIOD_ID: String = "period-1"
private const val SECOND_PERIOD_ID: String = "period-2"

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
        sectionRepository.save(Section(SECTION_ID, SCHOOL_YEAR_ID, Grade.THIRD, "3ro A"))
        periodRepository.saveAll(
            listOf(
                Period(FIRST_PERIOD_ID, SCHOOL_YEAR_ID, 1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 15)),
                Period(SECOND_PERIOD_ID, SCHOOL_YEAR_ID, 2, LocalDate.of(2026, 5, 16), LocalDate.of(2026, 7, 31)),
            ),
        )
    }

    @Test
    fun `creating an activity derives its period from the date`() = runTest {
        val activity: Activity = saveActivity(
            sectionId = SECTION_ID,
            activityId = null,
            name = "Debate del aula",
            date = LocalDate.of(2026, 3, 10),
            competencyIds = setOf("PPSS-1"),
        )

        assertThat(activity.periodId).isEqualTo(FIRST_PERIOD_ID)
        assertThat(getActivities(SECTION_ID, FIRST_PERIOD_ID).first()).containsExactly(activity)
    }

    @Test
    fun `changing the date moves the activity to another period`() = runTest {
        val created: Activity = saveActivity(
            sectionId = SECTION_ID,
            activityId = null,
            name = "Debate del aula",
            date = LocalDate.of(2026, 3, 10),
            competencyIds = setOf("PPSS-1"),
        )

        val moved: Activity = saveActivity(
            sectionId = SECTION_ID,
            activityId = created.id,
            name = created.name,
            date = LocalDate.of(2026, 6, 1),
            competencyIds = created.competencyIds,
        )

        assertThat(moved.periodId).isEqualTo(SECOND_PERIOD_ID)
        assertThat(getActivities(SECTION_ID, FIRST_PERIOD_ID).first()).isEmpty()
        assertThat(getActivities(SECTION_ID, SECOND_PERIOD_ID).first()).containsExactly(moved)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a date outside every period is rejected`() = runTest {
        saveActivity(
            sectionId = SECTION_ID,
            activityId = null,
            name = "Debate del aula",
            date = LocalDate.of(2027, 1, 1),
            competencyIds = setOf("PPSS-1"),
        )
    }

    @Test
    fun `recording an evidence level stores it`() = runTest {
        val activity: Activity = createActivity()
        val key = EvidenceLevelKey(activity.id, "student-1", "PPSS-1")

        recordEvidenceLevel(key, AchievementLevel.B)

        val stored: List<EvidenceLevel> = getEvidenceForActivity(activity.id).first()
        assertThat(stored).containsExactly(EvidenceLevel(key, AchievementLevel.B))
    }

    @Test
    fun `evidence levels are optional per student and competency`() = runTest {
        val activity: Activity = createActivity()
        recordEvidenceLevel(EvidenceLevelKey(activity.id, "student-1", "PPSS-1"), AchievementLevel.A)

        val stored: List<EvidenceLevel> = getEvidenceForActivity(activity.id).first()

        assertThat(stored.map { it.key.studentId }).containsExactly("student-1")
    }

    @Test
    fun `recording no level clears a previously recorded one`() = runTest {
        val activity: Activity = createActivity()
        val key = EvidenceLevelKey(activity.id, "student-1", "PPSS-1")
        recordEvidenceLevel(key, AchievementLevel.A)

        recordEvidenceLevel(key, null)

        assertThat(getEvidenceForActivity(activity.id).first()).isEmpty()
    }

    @Test
    fun `deleting an activity deletes its evidence levels`() = runTest {
        val activity: Activity = createActivity()
        recordEvidenceLevel(EvidenceLevelKey(activity.id, "student-1", "PPSS-1"), AchievementLevel.A)

        deleteActivity(activity.id)

        assertThat(getEvidenceForActivity(activity.id).first()).isEmpty()
        assertThat(getActivities(SECTION_ID, activity.periodId).first()).isEmpty()
    }

    @Test
    fun `editing an activity never touches period levels`() = runTest {
        val activity: Activity = createActivity()
        val periodLevelKey = PeriodLevelKey(SECTION_ID, activity.periodId, "student-1", "PPSS-1")
        savePeriodLevel(PeriodLevel(periodLevelKey).withAchievementLevel(AchievementLevel.B))

        saveActivity(
            sectionId = SECTION_ID,
            activityId = activity.id,
            name = "Nuevo nombre",
            date = activity.date,
            competencyIds = activity.competencyIds,
        )

        assertThat(periodLevelRepository.observeByPeriod(SECTION_ID, activity.periodId).first())
            .containsExactly(PeriodLevel(periodLevelKey).withAchievementLevel(AchievementLevel.B))
    }

    @Test
    fun `deleting an activity never touches period levels`() = runTest {
        val activity: Activity = createActivity()
        val periodLevelKey = PeriodLevelKey(SECTION_ID, activity.periodId, "student-1", "PPSS-1")
        savePeriodLevel(PeriodLevel(periodLevelKey).withAchievementLevel(AchievementLevel.B))

        deleteActivity(activity.id)

        assertThat(periodLevelRepository.observeByPeriod(SECTION_ID, activity.periodId).first())
            .containsExactly(PeriodLevel(periodLevelKey).withAchievementLevel(AchievementLevel.B))
    }

    @Test
    fun `the evidence count is the number of distinct students recorded`() = runTest {
        val activity: Activity = createActivity()
        recordEvidenceLevel(EvidenceLevelKey(activity.id, "student-1", "PPSS-1"), AchievementLevel.A)
        recordEvidenceLevel(EvidenceLevelKey(activity.id, "student-2", "PPSS-1"), AchievementLevel.B)

        val counts: Map<String, Int> = getEvidenceStudentCounts(SECTION_ID, activity.periodId).first()

        assertThat(counts[activity.id]).isEqualTo(2)
    }

    @Test
    fun `evidence for a period level is ordered by activity date`() = runTest {
        val first: Activity = createActivity()
        val second: Activity = saveActivity(
            sectionId = SECTION_ID,
            activityId = null,
            name = "Ficha de convivencia",
            date = LocalDate.of(2026, 3, 20),
            competencyIds = setOf("PPSS-1"),
        )
        recordEvidenceLevel(EvidenceLevelKey(second.id, "student-1", "PPSS-1"), AchievementLevel.B)
        recordEvidenceLevel(EvidenceLevelKey(first.id, "student-1", "PPSS-1"), AchievementLevel.A)

        val evidence: List<EvidenceRecord> =
            getEvidenceForPeriodLevel(SECTION_ID, first.periodId, "student-1", "PPSS-1").first()

        assertThat(evidence.map { it.activityId }).containsExactly(first.id, second.id).inOrder()
    }

    private suspend fun createActivity(): Activity = saveActivity(
        sectionId = SECTION_ID,
        activityId = null,
        name = "Debate del aula",
        date = LocalDate.of(2026, 3, 10),
        competencyIds = setOf("PPSS-1"),
    )
}
