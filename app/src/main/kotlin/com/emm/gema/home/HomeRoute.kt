package com.emm.gema.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@Composable
fun HomeRoute(
    onSectionForm: (String, String?) -> Unit,
    onSectionDetail: (String) -> Unit,
    onAttendanceDay: (String, LocalDate) -> Unit,
    onSchoolYears: () -> Unit,
    onPeriods: (String) -> Unit,
    onNavigateToBackup: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state: State<HomeUiState> = viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is HomeUiEffect.NavigateToSectionForm -> onSectionForm(effect.schoolYearId, effect.sectionId)
                is HomeUiEffect.NavigateToSectionDetail -> onSectionDetail(effect.sectionId)
                is HomeUiEffect.NavigateToAttendanceDay -> onAttendanceDay(effect.sectionId, effect.date)
                HomeUiEffect.NavigateToSchoolYears -> onSchoolYears()
                is HomeUiEffect.NavigateToPeriods -> onPeriods(effect.schoolYearId)
                HomeUiEffect.NavigateToBackup -> onNavigateToBackup()
            }
        }
    }

    HomeScreen(state = state.value, onIntent = viewModel::onIntent, modifier = modifier)
}
