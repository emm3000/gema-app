package com.emm.gema.feature.setup.section

import app.cash.turbine.test
import com.emm.gema.core.domain.id.IdGenerator
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.divide
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.setup.CompleteSetupUseCase
import com.emm.gema.core.domain.setup.SetupRepository
import com.emm.gema.feature.setup.MainDispatcherRule
import com.emm.gema.feature.setup.year.SchoolYearDraft
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class SetupSectionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val draft = SchoolYearDraft(
        label = "2026",
        startDate = LocalDate.of(2026, 3, 2),
        endDate = LocalDate.of(2026, 12, 18),
        periodKind = PeriodKind.BIMESTER,
        periods = PeriodKind.BIMESTER.divide(LocalDate.of(2026, 3, 2), LocalDate.of(2026, 12, 18)),
    )
    private val repository = RecordingSetupRepository()
    private val viewModel = SetupSectionViewModel(
        draft = draft,
        completeSetup = CompleteSetupUseCase(repository, IdGenerator { "id-${repository.identifiers++}" }),
    )

    @Test
    fun `the form starts empty and cannot finish`() {
        assertThat(viewModel.state.value.isLoading).isFalse()
        assertThat(viewModel.state.value.grade).isNull()
        assertThat(viewModel.state.value.canFinish).isFalse()
    }

    @Test
    fun `a grade and a name are enough to finish`() {
        fillSection()

        assertThat(viewModel.state.value.canFinish).isTrue()
        assertThat(viewModel.state.value.sectionNameError).isNull()
    }

    @Test
    fun `a blank name is reported and blocks finishing`() {
        fillSection()

        viewModel.onIntent(SetupSectionUiIntent.SectionNameChanged("   "))

        assertThat(viewModel.state.value.sectionNameError).isNotNull()
        assertThat(viewModel.state.value.canFinish).isFalse()
    }

    @Test
    fun `finishing persists the year, its periods and the section, then goes home`() = runTest {
        fillSection()

        viewModel.effects.test {
            viewModel.onIntent(SetupSectionUiIntent.FinishClicked)

            assertThat(awaitItem()).isEqualTo(SetupSectionUiEffect.NavigateToHome)
        }

        assertThat(repository.savedSchoolYear?.label).isEqualTo("2026")
        assertThat(repository.savedPeriods).hasSize(PeriodKind.BIMESTER.periodCount)
        assertThat(repository.savedSection?.name).isEqualTo("A")
        assertThat(repository.savedSection?.grade).isEqualTo(Grade.THIRD)
        assertThat(viewModel.state.value.isSaving).isFalse()
    }

    @Test
    fun `choosing the areas persists the setup first and carries the new section`() = runTest {
        fillSection()

        viewModel.effects.test {
            viewModel.onIntent(SetupSectionUiIntent.AreaSelectionClicked)

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(SetupSectionUiEffect.NavigateToSectionAreas::class.java)
            assertThat((effect as SetupSectionUiEffect.NavigateToSectionAreas).sectionId)
                .isEqualTo(repository.savedSection?.id)
        }
    }

    @Test
    fun `an incomplete form finishes nothing`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(SetupSectionUiIntent.FinishClicked)

            expectNoEvents()
        }
        assertThat(repository.savedSchoolYear).isNull()
    }

    @Test
    fun `a failing save is reported and lets the teacher try again`() = runTest {
        repository.failsOnce = true
        fillSection()

        viewModel.effects.test {
            viewModel.onIntent(SetupSectionUiIntent.FinishClicked)

            assertThat(awaitItem()).isInstanceOf(SetupSectionUiEffect.ShowMessage::class.java)
        }
        assertThat(viewModel.state.value.isSaving).isFalse()
        assertThat(viewModel.state.value.canFinish).isTrue()
    }

    @Test
    fun `going back returns to the year step`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(SetupSectionUiIntent.BackClicked)

            assertThat(awaitItem()).isEqualTo(SetupSectionUiEffect.NavigateBack)
        }
    }

    private fun fillSection() {
        viewModel.onIntent(SetupSectionUiIntent.GradeSelected(Grade.THIRD))
        viewModel.onIntent(SetupSectionUiIntent.SectionNameChanged("A"))
    }

    private class RecordingSetupRepository : SetupRepository {

        var identifiers: Int = 1
        var failsOnce: Boolean = false
        var savedSchoolYear: SchoolYear? = null
        var savedPeriods: List<Period> = emptyList()
        var savedSection: Section? = null

        override suspend fun saveAndActivate(
            schoolYear: SchoolYear,
            periods: List<Period>,
            section: Section,
        ) {
            if (failsOnce) {
                failsOnce = false
                error("the disk is full")
            }
            savedSchoolYear = schoolYear
            savedPeriods = periods
            savedSection = section
        }
    }
}
