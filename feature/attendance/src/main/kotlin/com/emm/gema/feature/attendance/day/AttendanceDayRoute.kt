package com.emm.gema.feature.attendance.day

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.gema.feature.attendance.R
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
    val messages: Map<AttendanceDayMessage, String> = attendanceDayMessages()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is AttendanceDayUiEffect.ShowMessage -> message = messages.getValue(effect.message)
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

@Composable
private fun attendanceDayMessages(): Map<AttendanceDayMessage, String> = mapOf(
    AttendanceDayMessage.FUTURE_DATE to stringResource(R.string.attendance_day_message_future_date),
    AttendanceDayMessage.RECORD_FAILED to stringResource(R.string.attendance_day_message_record_failed),
)
