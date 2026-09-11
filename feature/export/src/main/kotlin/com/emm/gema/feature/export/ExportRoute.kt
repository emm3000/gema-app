package com.emm.gema.feature.export

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ExportRoute(
    sectionId: String,
    onPeriodLevels: (String) -> Unit,
    onStudents: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExportViewModel = koinViewModel { parametersOf(sectionId) },
) {
    val state: State<ExportUiState> = viewModel.state.collectAsStateWithLifecycle()
    val context: Context = LocalContext.current
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val shareTitle: String = stringResource(R.string.export_share_title)
    val messages: Map<ExportMessage, String> = exportMessages()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect: ExportUiEffect ->
            when (effect) {
                is ExportUiEffect.ShareFile -> context.shareExport(effect.path, effect.mimeType, shareTitle)
                is ExportUiEffect.NavigateToPeriodLevels -> onPeriodLevels(effect.sectionId)
                is ExportUiEffect.NavigateToStudents -> onStudents(effect.sectionId)
                is ExportUiEffect.ShowMessage -> snackbarHostState.showSnackbar(messages.getValue(effect.message))
                ExportUiEffect.NavigateBack -> onBack()
            }
        }
    }

    ExportScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun exportMessages(): Map<ExportMessage, String> = mapOf(
    ExportMessage.EXPORT_FAILED to stringResource(R.string.export_message_failed),
    ExportMessage.EXPORT_UNAVAILABLE to stringResource(R.string.export_message_unavailable),
)
