package com.emm.gema.feature.evaluation.levels

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
fun PeriodLevelsRoute(
    sectionId: String,
    onWorkedCompetencies: (String, String, Area) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    studentId: String? = null,
    competencyId: String? = null,
) {
    val viewModel: PeriodLevelsViewModel = koinViewModel {
        parametersOf(sectionId, studentId.orEmpty(), competencyId.orEmpty())
    }
    val state: State<PeriodLevelsUiState> = viewModel.state.collectAsStateWithLifecycle()
    var message: String? by remember { mutableStateOf(null) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is PeriodLevelsUiEffect.NavigateToWorkedCompetencies ->
                    onWorkedCompetencies(effect.sectionId, effect.periodId, effect.area)

                PeriodLevelsUiEffect.NavigateBack -> onBack()
                is PeriodLevelsUiEffect.ShowMessage -> message = effect.text
            }
        }
    }

    PeriodLevelsScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        message = message,
        onMessageDismissed = { message = null },
    )
}
