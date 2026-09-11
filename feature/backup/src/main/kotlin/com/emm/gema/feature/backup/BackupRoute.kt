package com.emm.gema.feature.backup

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.gema.core.ui.share.shareFile
import java.io.File
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun BackupRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BackupViewModel = koinViewModel(),
) {
    val state: State<BackupUiState> = viewModel.state.collectAsStateWithLifecycle()
    val context: Context = LocalContext.current
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }

    val pickBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            viewModel.onIntent(BackupUiIntent.RestoreFilePicked(uri.toString()))
        }
    }

    val shareTitle: String = stringResource(R.string.backup_share_title)
    val messages: Map<BackupMessage, String> = backupMessages()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is BackupUiEffect.ShareFile -> context.shareFile(File(effect.path), effect.mimeType, shareTitle)
                is BackupUiEffect.OpenDocumentPicker -> pickBackup.launch(effect.mimeTypes.toTypedArray())
                is BackupUiEffect.ShowMessage -> snackbarHostState.showSnackbar(messages.getValue(effect.message))
                BackupUiEffect.RestartApp -> when (context.restart()) {
                    RestartOutcome.Restarted -> Unit
                    RestartOutcome.ManualRestartRequired ->
                        snackbarHostState.showSnackbar(messages.getValue(BackupMessage.MANUAL_RESTART_REQUIRED))
                }
                BackupUiEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    BackupScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun backupMessages(): Map<BackupMessage, String> = mapOf(
    BackupMessage.BACKUP_CREATED to stringResource(R.string.backup_message_created),
    BackupMessage.BACKUP_FAILED to stringResource(R.string.backup_message_failed),
    BackupMessage.FILE_IS_NOT_A_BACKUP to stringResource(R.string.backup_message_not_a_backup),
    BackupMessage.BACKUP_FROM_A_NEWER_APP to stringResource(R.string.backup_message_newer_app),
    BackupMessage.RESTORE_FAILED to stringResource(R.string.backup_message_restore_failed),
    BackupMessage.MANUAL_RESTART_REQUIRED to stringResource(R.string.backup_message_manual_restart_required),
)
