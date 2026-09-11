package com.emm.gema.feature.students.siagie

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.gema.feature.students.R
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ImportPreviewRoute(
    sectionId: String,
    uri: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ImportPreviewViewModel = koinViewModel { parametersOf(sectionId, uri) }
    val state: State<ImportPreviewUiState> = viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val context: Context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                ImportPreviewUiEffect.NavigateBack -> onBack()
                is ImportPreviewUiEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(context.stringOf(effect.message))
            }
        }
    }

    ImportPreviewScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}

private fun Context.stringOf(message: ImportPreviewMessage): String = when (message) {
    ImportPreviewMessage.ImportFailed -> getString(R.string.import_preview_message_failed)
    is ImportPreviewMessage.Applied ->
        getString(R.string.import_preview_message_applied, message.created, message.updated, message.withdrawn)
}
