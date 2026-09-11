package com.emm.gema.core.domain.setup

import com.emm.gema.core.domain.fake.InMemoryActiveSchoolYearRepository
import com.emm.gema.core.domain.fake.InMemoryPeriodRepository
import com.emm.gema.core.domain.fake.InMemorySchoolYearRepository
import com.emm.gema.core.domain.fake.InMemorySectionRepository
import com.emm.gema.core.domain.fake.InMemorySetupRepository
import com.emm.gema.core.domain.fake.SequentialIdGenerator
import com.emm.gema.core.domain.schoolyear.GetActiveSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.GetPeriodsUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearsUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodDates
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SwitchSchoolYearUseCase
import com.emm.gema.core.domain.section.GetSectionsUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

class CompleteSetupUseCaseTest {

    private val schoolYearRepository = InMemorySchoolYearRepository()
    private val periodRepository = InMemoryPeriodRepository()
    private val sectionRepository = InMemorySectionRepository()
    private val activeSchoolYearRepository = InMemoryActiveSchoolYearRepository()
    private val setupRepository = InMemorySetupRepository(
        schoolYearRepository = schoolYearRepository,
        periodRepository = periodRepository,
        sectionRepository = sectionRepository,
        activeSchoolYearRepository = activeSchoolYearRepository,
    )
    private val completeSetup = CompleteSetupUseCase(setupRepository, SequentialIdGenerator())
    private val getSchoolYears = GetSchoolYearsUseCase(schoolYearRepository)
    private val getPeriods = GetPeriodsUseCase(periodRepository)
    private val getSections = GetSectionsUseCase(sectionRepository)
    private val getActiveSchoolYear = GetActiveSchoolYearUseCase(
        activeSchoolYearRepository = activeSchoolYearRepository,
        schoolYearRepository = schoolYearRepository,
    )
    private val switchSchoolYear = SwitchSchoolYearUseCase(activeSchoolYearRepository)

    @Test
    fun `setup persists the school year, its periods and the first section`() = runTest {
        val completed: CompletedSetup = completeSetup(
            CompleteSetupRequest(
                yearLabel = "2026",
                startDate = LocalDate.of(2026, 3, 2),
                endDate = LocalDate.of(2026, 12, 18),
                periodKind = PeriodKind.BIMESTER,
                grade = Grade.THIRD,
                sectionName = "A",
            ),
        )

        assertThat(getSchoolYears().first()).containsExactly(completed.schoolYear)
        assertThat(getPeriods(completed.schoolYear.id).first()).hasSize(PeriodKind.BIMESTER.periodCount)
        assertThat(getSections(completed.schoolYear.id).first().single().name).isEqualTo("A")
    }

    @Test
    fun `the periods cover the school year without gaps`() = runTest {
        val completed: CompletedSetup = completeSetup(
            CompleteSetupRequest(
                yearLabel = "2026",
                startDate = LocalDate.of(2026, 3, 2),
                endDate = LocalDate.of(2026, 12, 18),
                periodKind = PeriodKind.TRIMESTER,
                grade = Grade.FIRST,
                sectionName = "A",
            ),
        )

        val periods: List<Period> = getPeriods(completed.schoolYear.id).first()

        assertThat(periods.map { it.number }).containsExactly(1, 2, 3).inOrder()
        assertThat(periods.first().startDate).isEqualTo(completed.schoolYear.startDate)
        assertThat(periods.last().endDate).isEqualTo(completed.schoolYear.endDate)
    }

    @Test
    fun `setup activates the school year it creates`() = runTest {
        val completed: CompletedSetup = completeSetup(
            CompleteSetupRequest(
                yearLabel = "2026",
                startDate = LocalDate.of(2026, 3, 2),
                endDate = LocalDate.of(2026, 12, 18),
                periodKind = PeriodKind.BIMESTER,
                grade = Grade.FIRST,
                sectionName = "A",
            ),
        )

        assertThat(getActiveSchoolYear().first()).isEqualTo(completed.schoolYear)
    }

    @Test
    fun `a second school year keeps the first one and its sections reachable`() = runTest {
        val first: CompletedSetup = completeSetup(
            CompleteSetupRequest(
                yearLabel = "2025",
                startDate = LocalDate.of(2025, 3, 3),
                endDate = LocalDate.of(2025, 12, 19),
                periodKind = PeriodKind.BIMESTER,
                grade = Grade.FIRST,
                sectionName = "A",
            ),
        )
        val second: CompletedSetup = completeSetup(
            CompleteSetupRequest(
                yearLabel = "2026",
                startDate = LocalDate.of(2026, 3, 2),
                endDate = LocalDate.of(2026, 12, 18),
                periodKind = PeriodKind.TRIMESTER,
                grade = Grade.SECOND,
                sectionName = "B",
            ),
        )

        assertThat(getSchoolYears().first()).containsExactly(second.schoolYear, first.schoolYear).inOrder()
        assertThat(getActiveSchoolYear().first()).isEqualTo(second.schoolYear)

        val firstSections: List<Section> = getSections(first.schoolYear.id).first()
        assertThat(firstSections.single().name).isEqualTo("A")
        assertThat(getPeriods(first.schoolYear.id).first()).hasSize(PeriodKind.BIMESTER.periodCount)

        switchSchoolYear(first.schoolYear.id)
        assertThat(getActiveSchoolYear().first()).isEqualTo(first.schoolYear)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `setup without a section name is rejected`() = runTest {
        completeSetup(
            CompleteSetupRequest(
                yearLabel = "2026",
                startDate = LocalDate.of(2026, 3, 2),
                endDate = LocalDate.of(2026, 12, 18),
                periodKind = PeriodKind.BIMESTER,
                grade = Grade.FIRST,
                sectionName = " ",
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `setup with overlapping period dates is rejected`() = runTest {
        completeSetup(
            CompleteSetupRequest(
                yearLabel = "2026",
                startDate = LocalDate.of(2026, 3, 2),
                endDate = LocalDate.of(2026, 12, 18),
                periodKind = PeriodKind.BIMESTER,
                grade = Grade.FIRST,
                sectionName = "A",
                periodDates = listOf(
                    PeriodDates(1, LocalDate.of(2026, 3, 2), LocalDate.of(2026, 6, 30)),
                    PeriodDates(2, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31)),
                    PeriodDates(3, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 31)),
                    PeriodDates(4, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 18)),
                ),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `setup with a period outside the school year is rejected`() = runTest {
        completeSetup(
            CompleteSetupRequest(
                yearLabel = "2026",
                startDate = LocalDate.of(2026, 3, 2),
                endDate = LocalDate.of(2026, 12, 18),
                periodKind = PeriodKind.TRIMESTER,
                grade = Grade.FIRST,
                sectionName = "A",
                periodDates = listOf(
                    PeriodDates(1, LocalDate.of(2026, 3, 2), LocalDate.of(2026, 6, 30)),
                    PeriodDates(2, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 30)),
                    PeriodDates(3, LocalDate.of(2026, 10, 1), LocalDate.of(2027, 1, 10)),
                ),
            ),
        )
    }
}
