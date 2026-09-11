package com.emm.gema.feature.sections.areas

import app.cash.turbine.test
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.GetSectionAreasUseCase
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SetAreaVisibilityUseCase
import com.emm.gema.feature.sections.FakeSectionAreaRepository
import com.emm.gema.feature.sections.FakeSectionRepository
import com.emm.gema.feature.sections.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SectionAreasViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val section = Section("section-1", "2026", Grade.THIRD, "A")
    private val sectionRepository = FakeSectionRepository(listOf(section))
    private val sectionAreaRepository = FakeSectionAreaRepository()
    private val viewModel: SectionAreasViewModel by lazy {
        SectionAreasViewModel(
            sectionId = section.id,
            getSection = GetSectionUseCase(sectionRepository),
            getSectionAreas = GetSectionAreasUseCase(sectionAreaRepository),
            setAreaVisibility = SetAreaVisibilityUseCase(sectionAreaRepository),
        )
    }

    @Test
    fun `every area starts active and is named for the teacher`() {
        val state: SectionAreasUiState = viewModel.state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.sectionTitle).isEqualTo("3° A")
        assertThat(state.areas.map { it.id }).containsExactlyElementsIn(Area.entries).inOrder()
        assertThat(state.areas.all { it.isActive }).isTrue()
        assertThat(state.areas.single { it.id == Area.MATE }.name).isEqualTo("Matemática")
    }

    @Test
    fun `turning an area off is saved without a save button`() {
        viewModel.onIntent(SectionAreasUiIntent.AreaToggled(Area.EFIS, isActive = false))

        assertThat(viewModel.state.value.areas.single { it.id == Area.EFIS }.isActive).isFalse()
        assertThat(sectionAreaRepository.hiddenAreas.value.getValue(section.id)).containsExactly(Area.EFIS)
    }

    @Test
    fun `turning an area back on restores it`() {
        viewModel.onIntent(SectionAreasUiIntent.AreaToggled(Area.EREL, isActive = false))
        viewModel.onIntent(SectionAreasUiIntent.AreaToggled(Area.EREL, isActive = true))

        assertThat(viewModel.state.value.areas.all { it.isActive }).isTrue()
        assertThat(sectionAreaRepository.hiddenAreas.value.getValue(section.id)).isEmpty()
    }

    @Test
    fun `going back leaves the screen`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(SectionAreasUiIntent.BackClicked)

            assertThat(awaitItem()).isEqualTo(SectionAreasUiEffect.NavigateBack)
        }
    }
}
