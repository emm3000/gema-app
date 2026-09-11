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
    private val updatePeriods = UpdatePeriodsUseCase(periodRepository, schoolYearRepository)

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
        val stored: List<Period> = getPeriods(schoolYear.id).first()

        updatePeriods(
            schoolYearId = schoolYear.id,
            periodDates = stored.map { PeriodDates(it.number, it.startDate, it.endDate) }
                .mapIndexed { index, dates ->
                    if (index == 0) dates.copy(endDate = dates.endDate.minusDays(10)) else dates
                },
        )

        assertThat(getPeriods(schoolYear.id).first().first().endDate)
            .isEqualTo(stored.first().endDate.minusDays(10))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `periods that overlap each other are rejected`() = runTest {
        val stored: List<Period> = getPeriods(schoolYear.id).first()

        updatePeriods(
            schoolYearId = schoolYear.id,
            periodDates = stored.map { PeriodDates(it.number, it.startDate, it.endDate) }
                .mapIndexed { index, dates ->
                    if (index == 0) dates.copy(endDate = stored[1].startDate) else dates
                },
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a period that leaves the school year is rejected`() = runTest {
        val stored: List<Period> = getPeriods(schoolYear.id).first()

        updatePeriods(
            schoolYearId = schoolYear.id,
            periodDates = stored.map { PeriodDates(it.number, it.startDate, it.endDate) }
                .mapIndexed { index, dates ->
                    if (index == 0) dates.copy(startDate = schoolYear.startDate.minusDays(3)) else dates
                },
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `editing a school year that does not exist is rejected`() = runTest {
        updatePeriods(schoolYearId = "missing", periodDates = emptyList())
    }

    private fun clockAt(date: LocalDate): Clock = Clock.fixed(
        date.atStartOfDay(ZoneOffset.UTC).toInstant(),
        ZoneId.of("UTC"),
    )
}
