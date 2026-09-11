package com.emm.gema.navigation

import com.emm.gema.MainDispatcherRule
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyRepository
import com.emm.gema.core.domain.curriculum.PrimaryCurriculum
import com.emm.gema.core.domain.curriculum.SeedCurriculumUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearsUseCase
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearRepository
import com.emm.gema.core.domain.section.Area
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class StartDestinationViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    private val competencyRepository: RecordingCompetencyRepository = RecordingCompetencyRepository()
    private val schoolYear: SchoolYear = SchoolYear(
        id = "year-1",
        label = "2026",
        startDate = LocalDate.of(2026, 3, 2),
        endDate = LocalDate.of(2026, 12, 18),
        periodKind = PeriodKind.BIMESTER,
    )

    @Test
    fun `a cold start seeds the curriculum before any screen opens`() = runTest {
        viewModel(schoolYears = emptyList())

        assertThat(competencyRepository.stored.map { it.id })
            .containsExactlyElementsIn(PrimaryCurriculum.competencies.map { it.id })
    }

    @Test
    fun `a device with no school year starts on the setup flow`() = runTest {
        val viewModel: StartDestinationViewModel = viewModel(schoolYears = emptyList())

        assertThat(viewModel.startDestination.value).isEqualTo(GemaRoutes.SETUP_YEAR)
    }

    @Test
    fun `a device with a school year starts on home`() = runTest {
        val viewModel: StartDestinationViewModel = viewModel(schoolYears = listOf(schoolYear))

        assertThat(viewModel.startDestination.value).isEqualTo(GemaRoutes.HOME)
    }

    private fun viewModel(schoolYears: List<SchoolYear>): StartDestinationViewModel = StartDestinationViewModel(
        getSchoolYears = GetSchoolYearsUseCase(FakeSchoolYearRepository(schoolYears)),
        seedCurriculum = SeedCurriculumUseCase(competencyRepository),
    )
}

private class RecordingCompetencyRepository : CompetencyRepository {

    val stored: MutableList<Competency> = mutableListOf()

    override suspend fun seed(competencies: List<Competency>, curriculumVersion: Int) {
        stored.clear()
        stored.addAll(competencies)
    }

    override suspend fun findByArea(area: Area): List<Competency> = stored.filter { it.area == area }
}

private class FakeSchoolYearRepository(private val schoolYears: List<SchoolYear>) : SchoolYearRepository {

    override fun observeAll(): Flow<List<SchoolYear>> = MutableStateFlow(schoolYears)

    override suspend fun findById(id: String): SchoolYear? = schoolYears.find { it.id == id }

    override suspend fun save(schoolYear: SchoolYear) = Unit
}
