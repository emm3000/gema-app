package com.emm.gema.feature.students.list

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.emm.gema.feature.students.R
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun StudentsRoute(
    sectionId: String,
    onStudentForm: (String, String?) -> Unit,
    onImportPreview: (String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: StudentsViewModel = koinViewModel { parametersOf(sectionId) }
    val pickTemplate = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) viewModel.onIntent(StudentsUiIntent.ImportFilePicked(uri.toString()))
    }
    val state: State<StudentsUiState> = viewModel.state.collectAsStateWithLifecycle()
    var message: String? by remember { mutableStateOf(null) }
    val messages: Map<StudentsMessage, String> = studentsMessages()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is StudentsUiEffect.NavigateToStudentForm -> onStudentForm(effect.sectionId, effect.studentId)
                is StudentsUiEffect.OpenDocumentPicker -> pickTemplate.launch(effect.mimeTypes.toTypedArray())
                is StudentsUiEffect.NavigateToImportPreview -> onImportPreview(effect.sectionId, effect.uri)
                StudentsUiEffect.NavigateBack -> onBack()
                is StudentsUiEffect.ShowMessage -> message = messages.getValue(effect.message)
            }
        }
    }

    StudentsScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        message = message,
        onMessageDismissed = { message = null },
    )
}

@Composable
private fun studentsMessages(): Map<StudentsMessage, String> = mapOf(
    StudentsMessage.REACTIVATE_FAILED to stringResource(R.string.students_message_reactivate_failed),
)
