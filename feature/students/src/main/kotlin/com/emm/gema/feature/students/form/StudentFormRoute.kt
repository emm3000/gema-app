package com.emm.gema.feature.students.form

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
fun StudentFormRoute(
    sectionId: String,
    studentId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: StudentFormViewModel = koinViewModel { parametersOf(sectionId, studentId) }
    val state: State<StudentFormUiState> = viewModel.state.collectAsStateWithLifecycle()
    var message: String? by remember { mutableStateOf(null) }
    val messages: Map<StudentFormMessage, String> = studentFormMessages()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                StudentFormUiEffect.NavigateBack -> onBack()
                is StudentFormUiEffect.ShowMessage -> message = messages.getValue(effect.message)
            }
        }
    }

    StudentFormScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        message = message,
        onMessageDismissed = { message = null },
    )
}

@Composable
private fun studentFormMessages(): Map<StudentFormMessage, String> = mapOf(
    StudentFormMessage.SAVE_FAILED to stringResource(R.string.student_form_message_save_failed),
)
