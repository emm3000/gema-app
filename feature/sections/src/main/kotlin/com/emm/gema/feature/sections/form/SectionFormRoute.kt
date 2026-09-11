package com.emm.gema.feature.sections.form

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
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.emm.gema.feature.sections.R

@Composable
fun SectionFormRoute(
    schoolYearId: String,
    sectionId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SectionFormViewModel = koinViewModel { parametersOf(schoolYearId, sectionId) }
    val state: State<SectionFormUiState> = viewModel.state.collectAsStateWithLifecycle()
    var message: SectionFormMessage? by remember { mutableStateOf(null) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                SectionFormUiEffect.NavigateBack -> onBack()
                is SectionFormUiEffect.ShowMessage -> message = effect.message
            }
        }
    }

    SectionFormScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        message = message?.let { stringResource(sectionFormMessageRes(it)) },
        onMessageDismissed = { message = null },
    )
}

internal fun sectionFormMessageRes(message: SectionFormMessage): Int = when (message) {
    SectionFormMessage.MISSING_NAME -> R.string.sections_form_message_missing_name
    SectionFormMessage.SAVE_FAILED -> R.string.sections_form_message_save_failed
    SectionFormMessage.DELETE_FAILED -> R.string.sections_form_message_delete_failed
}
