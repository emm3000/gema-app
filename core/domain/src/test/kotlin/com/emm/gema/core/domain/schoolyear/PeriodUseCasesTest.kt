package com.emm.gema.core.domain.schoolyear

import com.emm.gema.core.domain.fake.InMemoryPeriodRepository
import com.emm.gema.core.domain.fake.InMemorySchoolYearRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class PeriodUseCasesTest {

    private val schoolYear = SchoolYear(
        id = "2026",
        label = "2026",
        startDate = LocalDate.of(2026, 1, 1),
        endDate = LocalDate.of(2026, 12, 31),
        periodKind = PeriodKind.BIMESTER,
    )
    private val schoolYearRepository = InMemorySchoolYearRepository()
    private val periodRepository = InMemoryPeriodRepository()
    private val getPeriods = GetPeriodsUseCase(periodRepository)
    private val updatePeriodDates = UpdatePeriodDatesUseCase(periodRepository, schoolYearRepository)

    @Before
    fun seedSchoolYear() = runTest {
        schoolYearRepository.save(schoolYear)
        periodRepository.saveAll(
            PeriodKind.BIMESTER.divide(schoolYear.startDate, schoolYear.endDate).map { dates ->
                Period(
                    id = "period-${dates.number}",
                    schoolYearId = schoolYear.id,
                    number = dates.number,
                    startDate = dates.startDate,
                    endDate = dates.endDate,
                )
            }
        )
    }

    @Test
    fun `the current period is the one that contains today`() = runTest {
        val getCurrentPeriod = GetCurrentPeriodUseCase(periodRepository, clockAt(LocalDate.of(2026, 5, 10)))

        val current: Period? = getCurrentPeriod(schoolYear.id)

        assertThat(current?.number).isEqualTo(2)
    }

    @Test
    fun `there is no current period when today falls outside the school year`() = runTest {
        val getCurrentPeriod = GetCurrentPeriodUseCase(periodRepository, clockAt(LocalDate.of(2027, 1, 5)))

        assertThat(getCurrentPeriod(schoolYear.id)).isNull()
    }

    @Test
    fun `the first day of a period already belongs to it`() = runTest {
        val secondPeriodStart: LocalDate = getPeriods(schoolYear.id).first()[1].startDate
        val getCurrentPeriod = GetCurrentPeriodUseCase(periodRepository, clockAt(secondPeriodStart))

        assertThat(getCurrentPeriod(schoolYear.id)?.number).isEqualTo(2)
    }

    @Test
    fun `edited period dates are kept`() = runTest {
        updatePeriodDates(
            periodId = "period-1",
            startDate = LocalDate.of(2026, 1, 5),
            endDate = LocalDate.of(2026, 3, 20),
        )

        val first: Period = getPeriods(schoolYear.id).first().first()
        assertThat(first.startDate).isEqualTo(LocalDate.of(2026, 1, 5))
        assertThat(first.endDate).isEqualTo(LocalDate.of(2026, 3, 20))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a period that overlaps another one is rejected`() = runTest {
        val second: Period = getPeriods(schoolYear.id).first()[1]

        updatePeriodDates(
            periodId = "period-1",
            startDate = LocalDate.of(2026, 1, 1),
            endDate = second.startDate,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a period that leaves the school year is rejected`() = runTest {
        updatePeriodDates(
            periodId = "period-1",
            startDate = LocalDate.of(2025, 12, 28),
            endDate = LocalDate.of(2026, 2, 20),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `editing a period that does not exist is rejected`() = runTest {
        updatePeriodDates(
            periodId = "missing",
            startDate = LocalDate.of(2026, 1, 1),
            endDate = LocalDate.of(2026, 2, 1),
        )
    }

    private fun clockAt(date: LocalDate): Clock = Clock.fixed(
        date.atStartOfDay(ZoneOffset.UTC).toInstant(),
        ZoneId.of("UTC"),
    )
}
