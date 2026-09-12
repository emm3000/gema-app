package com.emm.gema.core.database.setup

import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.UuidIdGenerator
import com.emm.gema.core.database.inMemoryGemaDb
import com.emm.gema.core.database.schoolyear.SqlDelightActiveSchoolYearRepository
import com.emm.gema.core.database.schoolyear.SqlDelightPeriodRepository
import com.emm.gema.core.database.schoolyear.SqlDelightSchoolYearRepository
import com.emm.gema.core.database.section.SqlDelightSectionAreaRepository
import com.emm.gema.core.database.section.SqlDelightSectionCascade
import com.emm.gema.core.database.section.SqlDelightSectionRepository
import com.emm.gema.core.domain.schoolyear.ActiveSchoolYearRepository
import com.emm.gema.core.domain.schoolyear.GetActiveSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.GetCurrentPeriodUseCase
import com.emm.gema.core.domain.schoolyear.GetPeriodsUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearsUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.PeriodRepository
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearRepository
import com.emm.gema.core.domain.schoolyear.SwitchSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.PeriodDates
import com.emm.gema.core.domain.schoolyear.UpdatePeriodsUseCase
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.CreateSectionUseCase
import com.emm.gema.core.domain.section.DeleteSectionUseCase
import com.emm.gema.core.domain.section.GetSectionAreasUseCase
import com.emm.gema.core.domain.section.GetSectionsUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionAreaRepository
import com.emm.gema.core.domain.section.SectionRepository
import com.emm.gema.core.domain.section.SetAreaVisibilityUseCase
import com.emm.gema.core.domain.setup.CompleteSetupRequest
import com.emm.gema.core.domain.setup.CompleteSetupUseCase
import com.emm.gema.core.domain.setup.SetupRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class SetupPersistenceTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val database: GemaDb = inMemoryGemaDb()
    private val schoolYearRepository: SchoolYearRepository = SqlDelightSchoolYearRepository(database, dispatcher)
    private val periodRepository: PeriodRepository = SqlDelightPeriodRepository(database, dispatcher)
    private val sectionRepository: SectionRepository = SqlDelightSectionRepository(database, dispatcher)
    private val sectionAreaRepository: SectionAreaRepository = SqlDelightSectionAreaRepository(database, dispatcher)
    private val activeSchoolYearRepository: ActiveSchoolYearRepository =
        SqlDelightActiveSchoolYearRepository(database, dispatcher)
    private val setupRepository: SetupRepository = SqlDelightSetupRepository(database, dispatcher)

    private val completeSetup = CompleteSetupUseCase(setupRepository, UuidIdGenerator())
    private val getSchoolYears = GetSchoolYearsUseCase(schoolYearRepository)
    private val getPeriods = GetPeriodsUseCase(periodRepository)
    private val getSections = GetSectionsUseCase(sectionRepository)
    private val getSectionAreas = GetSectionAreasUseCase(sectionAreaRepository)
    private val setAreaVisibility = SetAreaVisibilityUseCase(sectionAreaRepository)
    private val createSection = CreateSectionUseCase(sectionRepository, UuidIdGenerator())
    private val deleteSection = DeleteSectionUseCase(SqlDelightSectionCascade(database, dispatcher))
    private val switchSchoolYear = SwitchSchoolYearUseCase(activeSchoolYearRepository)
    private val getActiveSchoolYear = GetActiveSchoolYearUseCase(activeSchoolYearRepository, schoolYearRepository)
    private val updatePeriods = UpdatePeriodsUseCase(periodRepository, schoolYearRepository)

    @Test
    fun `setup persists the school year, its periods and its first section`() = runTest {
        val schoolYear: SchoolYear = runSetup()

        assertThat(getSchoolYears().first()).containsExactly(schoolYear)
        assertThat(getPeriods(schoolYear.id).first().map { it.number }).containsExactly(1, 2, 3, 4).inOrder()
        assertThat(getSections(schoolYear.id).first().single().name).isEqualTo("A")
        assertThat(getActiveSchoolYear().first()).isEqualTo(schoolYear)
    }

    @Test
    fun `the current period is derived from today and the stored period dates`() = runTest {
        val schoolYear: SchoolYear = runSetup()
        val second: Period = getPeriods(schoolYear.id).first()[1]
        val getCurrentPeriod = GetCurrentPeriodUseCase(
            repository = periodRepository,
            clock = Clock.fixed(second.startDate.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneId.of("UTC")),
        )

        assertThat(getCurrentPeriod(schoolYear.id)).isEqualTo(second)
    }

    @Test
    fun `edited period dates are persisted`() = runTest {
        val schoolYear: SchoolYear = runSetup()
        val stored: List<Period> = getPeriods(schoolYear.id).first()

        updatePeriods(
            schoolYearId = schoolYear.id,
            periodDates = stored.mapIndexed { index, period ->
                val dates = PeriodDates(period.number, period.startDate, period.endDate)
                if (index == 0) dates.copy(endDate = dates.endDate.minusDays(5)) else dates
            },
        )

        assertThat(getPeriods(schoolYear.id).first().first().endDate)
            .isEqualTo(stored.first().endDate.minusDays(5))
    }

    @Test
    fun `a hidden area stays hidden and can be turned back on`() = runTest {
        val schoolYear: SchoolYear = runSetup()
        val section: Section = getSections(schoolYear.id).first().single()

        assertThat(getSectionAreas(section.id).first().all { it.isActive }).isTrue()

        setAreaVisibility(section.id, Area.EFIS, isActive = false)
        assertThat(getSectionAreas(section.id).first().single { it.area == Area.EFIS }.isActive).isFalse()

        setAreaVisibility(section.id, Area.EFIS, isActive = true)
        assertThat(getSectionAreas(section.id).first().all { it.isActive }).isTrue()
    }

    @Test
    fun `a deleted section takes its hidden areas with it`() = runTest {
        val schoolYear: SchoolYear = runSetup()
        val section: Section = createSection(schoolYear.id, Grade.SECOND, "B")
        setAreaVisibility(section.id, Area.ARTE, isActive = false)

        deleteSection(section.id)

        assertThat(getSections(schoolYear.id).first().map { it.name }).containsExactly("A")
        assertThat(getSectionAreas(section.id).first().all { it.isActive }).isTrue()
    }

    @Test
    fun `a second school year keeps the first one and its sections`() = runTest {
        val first: SchoolYear = runSetup()
        val second: SchoolYear = completeSetup(
            CompleteSetupRequest(
                yearLabel = "2027",
                startDate = LocalDate.of(2027, 3, 1),
                endDate = LocalDate.of(2027, 12, 17),
                periodKind = PeriodKind.TRIMESTER,
                grade = Grade.FOURTH,
                sectionName = "Unica",
            ),
        ).schoolYear

        assertThat(getSchoolYears().first()).containsExactly(second, first).inOrder()
        assertThat(getSections(first.id).first().single().name).isEqualTo("A")
        assertThat(getPeriods(first.id).first()).hasSize(4)
        assertThat(getActiveSchoolYear().first()).isEqualTo(second)

        switchSchoolYear(first.id)
        assertThat(getActiveSchoolYear().first()).isEqualTo(first)
    }

    private suspend fun runSetup(): SchoolYear = completeSetup(
        CompleteSetupRequest(
            yearLabel = "2026",
            startDate = LocalDate.of(2026, 3, 2),
            endDate = LocalDate.of(2026, 12, 18),
            periodKind = PeriodKind.BIMESTER,
            grade = Grade.THIRD,
            sectionName = "A",
        ),
    ).schoolYear
}
