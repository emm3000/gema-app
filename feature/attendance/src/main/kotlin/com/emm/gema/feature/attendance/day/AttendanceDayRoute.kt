package com.emm.gema.feature.attendance.day

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun AttendanceDayRoute(
    sectionId: String,
    date: LocalDate?,
    onBack: () -> Unit,
    onMonthlySummary: (String, YearMonth) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AttendanceDayViewModel = koinViewModel { parametersOf(sectionId, date) }
    val state: State<AttendanceDayUiState> = viewModel.state.collectAsStateWithLifecycle()
    var message: String? by remember { mutableStateOf(null) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is AttendanceDayUiEffect.ShowMessage -> message = effect.text
                is AttendanceDayUiEffect.NavigateToAttendanceMonth -> onMonthlySummary(effect.sectionId, effect.month)
                AttendanceDayUiEffect.NavigateBack -> onBack()
            }
        }
    }

    AttendanceDayScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        message = message,
        onMessageDismissed = { message = null },
    )
}
