package com.emm.gema.feature.sections.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.SectionId
import java.time.LocalDate
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SectionDetailRoute(
    sectionId: SectionId,
    onAttendanceDay: (String, LocalDate) -> Unit,
    onStudents: (SectionId) -> Unit,
    onPeriodLevels: (SectionId) -> Unit,
    onExport: (SectionId) -> Unit,
    onActivities: (SectionId) -> Unit,
    onSectionAreas: (SectionId) -> Unit,
    onSectionForm: (SchoolYearId, SectionId) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SectionDetailViewModel = koinViewModel { parametersOf(sectionId) }
    val state: State<SectionDetailUiState> = viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is SectionDetailUiEffect.NavigateToAttendanceDay ->
                    onAttendanceDay(effect.sectionId.value, effect.date)
                is SectionDetailUiEffect.NavigateToStudents -> onStudents(effect.sectionId)
                is SectionDetailUiEffect.NavigateToPeriodLevels -> onPeriodLevels(effect.sectionId)
                is SectionDetailUiEffect.NavigateToExport -> onExport(effect.sectionId)
                is SectionDetailUiEffect.NavigateToActivities -> onActivities(effect.sectionId)
                is SectionDetailUiEffect.NavigateToSectionAreas -> onSectionAreas(effect.sectionId)
                is SectionDetailUiEffect.NavigateToSectionForm ->
                    onSectionForm(effect.schoolYearId, effect.sectionId)
                SectionDetailUiEffect.NavigateBack -> onBack()
            }
        }
    }

    SectionDetailScreen(state = state.value, onIntent = viewModel::onIntent, modifier = modifier)
}
