package com.emm.gema.feature.setup.years

import app.cash.turbine.test
import com.emm.gema.core.domain.schoolyear.ActiveSchoolYearRepository
import com.emm.gema.core.domain.schoolyear.GetActiveSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearsUseCase
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.schoolyear.SchoolYearRepository
import com.emm.gema.core.domain.schoolyear.SwitchSchoolYearUseCase
import com.emm.gema.core.domain.section.GetSectionCountsUseCase
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.section.SectionRepository
import com.emm.gema.feature.setup.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SchoolYearsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val olderYear = SchoolYear(
        id = SchoolYearId("2025"),
        label = "2025",
        startDate = LocalDate.of(2025, 3, 3),
        endDate = LocalDate.of(2025, 12, 19),
        periodKind = PeriodKind.TRIMESTER,
    )
    private val currentYear = SchoolYear(
        id = SchoolYearId("2026"),
        label = "2026",
        startDate = LocalDate.of(2026, 3, 2),
        endDate = LocalDate.of(2026, 12, 18),
        periodKind = PeriodKind.BIMESTER,
    )
    private val schoolYearRepository = FakeSchoolYearRepository(listOf(currentYear, olderYear))
    private val sectionRepository = FakeSectionRepository(
        listOf(
            Section(SectionId("a"), currentYear.id, com.emm.gema.core.domain.section.Grade.FIRST, "A"),
            Section(SectionId("b"), currentYear.id, com.emm.gema.core.domain.section.Grade.SECOND, "B"),
            Section(SectionId("c"), olderYear.id, com.emm.gema.core.domain.section.Grade.THIRD, "C"),
        )
    )
    private val activeSchoolYearRepository = FakeActiveSchoolYearRepository(currentYear.id)
    private val viewModel: SchoolYearsViewModel by lazy {
        SchoolYearsViewModel(
            getSchoolYears = GetSchoolYearsUseCase(schoolYearRepository),
            getSectionCounts = GetSectionCountsUseCase(sectionRepository),
            getActiveSchoolYear = GetActiveSchoolYearUseCase(activeSchoolYearRepository, schoolYearRepository),
            switchSchoolYear = SwitchSchoolYearUseCase(activeSchoolYearRepository),
        )
    }

    @Test
    fun `every school year is listed with its sections and the active one is marked`() {
        val state: SchoolYearsUiState = viewModel.state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.years.map { it.id }).containsExactly(SchoolYearId("2026"), SchoolYearId("2025")).inOrder()
        assertThat(state.years.first().sectionCount).isEqualTo(2)
        assertThat(state.years.last().sectionCount).isEqualTo(1)
        assertThat(state.years.single { it.isActive }.id).isEqualTo(SchoolYearId("2026"))
    }

    @Test
    fun `a school year shows its date range and its period kind`() {
        val row: SchoolYearRow = viewModel.state.value.years.first()

        assertThat(row.startDate).isEqualTo(LocalDate.of(2026, 3, 2))
        assertThat(row.endDate).isEqualTo(LocalDate.of(2026, 12, 18))
        assertThat(row.periodKind).isEqualTo(PeriodKind.BIMESTER)
    }

    @Test
    fun `choosing a school year switches to it without losing the other one`() {
        viewModel.onIntent(SchoolYearsUiIntent.YearClicked(SchoolYearId("2025")))

        val state: SchoolYearsUiState = viewModel.state.value
        assertThat(state.years.single { it.isActive }.id).isEqualTo(SchoolYearId("2025"))
        assertThat(state.years.map { it.id }).containsExactly(SchoolYearId("2026"), SchoolYearId("2025")).inOrder()
    }

    @Test
    fun `the periods of a school year are one tap away`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(SchoolYearsUiIntent.PeriodsClicked(SchoolYearId("2025")))

            assertThat(awaitItem()).isEqualTo(SchoolYearsUiEffect.NavigateToPeriods(SchoolYearId("2025")))
        }
    }

    @Test
    fun `adding a year reuses the setup form`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(SchoolYearsUiIntent.AddYearClicked)

            assertThat(awaitItem()).isEqualTo(SchoolYearsUiEffect.NavigateToSetupYear)
        }
    }

    @Test
    fun `going back leaves the screen`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(SchoolYearsUiIntent.BackClicked)

            assertThat(awaitItem()).isEqualTo(SchoolYearsUiEffect.NavigateBack)
        }
    }

    private class FakeSchoolYearRepository(initial: List<SchoolYear>) : SchoolYearRepository {

        private val schoolYears: MutableStateFlow<List<SchoolYear>> = MutableStateFlow(initial)

        override fun observeAll(): Flow<List<SchoolYear>> = schoolYears

        override suspend fun findById(id: SchoolYearId): SchoolYear? = schoolYears.value.find { it.id == id }

        override suspend fun save(schoolYear: SchoolYear) = Unit
    }

    private class FakeSectionRepository(initial: List<Section>) : SectionRepository {

        private val sections: MutableStateFlow<List<Section>> = MutableStateFlow(initial)

        override fun observeBySchoolYear(schoolYearId: SchoolYearId): Flow<List<Section>> = sections
            .map { stored -> stored.filter { it.schoolYearId == schoolYearId } }

        override fun observeCountsBySchoolYear(): Flow<Map<SchoolYearId, Int>> = sections
            .map { stored -> stored.groupingBy { it.schoolYearId }.eachCount() }

        override suspend fun findById(id: SectionId): Section? = sections.value.find { it.id == id }

        override suspend fun save(section: Section) = Unit

        override suspend fun delete(id: SectionId) = Unit
    }

    private class FakeActiveSchoolYearRepository(initial: SchoolYearId?) : ActiveSchoolYearRepository {

        private val activeId: MutableStateFlow<SchoolYearId?> = MutableStateFlow(initial)

        override fun observeActiveId(): Flow<SchoolYearId?> = activeId

        override suspend fun activate(schoolYearId: SchoolYearId) {
            activeId.value = schoolYearId
        }
    }
}
