package com.emm.gema.feature.evaluation.worked

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.gema.core.domain.section.Area
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun WorkedCompetenciesRoute(
    sectionId: String,
    periodId: String,
    area: Area,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: WorkedCompetenciesViewModel = koinViewModel { parametersOf(sectionId, periodId, area) }
    val state: State<WorkedCompetenciesUiState> = viewModel.state.collectAsStateWithLifecycle()
    var message: String? by remember { mutableStateOf(null) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                WorkedCompetenciesUiEffect.NavigateBack -> onBack()
                is WorkedCompetenciesUiEffect.ShowMessage -> message = effect.text
            }
        }
    }

    WorkedCompetenciesScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        message = message,
        onMessageDismissed = { message = null },
    )
}
