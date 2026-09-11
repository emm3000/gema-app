package com.emm.gema.feature.evaluation.levels

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
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.feature.evaluation.R
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PeriodLevelsRoute(
    sectionId: SectionId,
    onWorkedCompetencies: (SectionId, PeriodId, Area) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    studentId: StudentId? = null,
    competencyId: CompetencyId? = null,
) {
    val viewModel: PeriodLevelsViewModel = koinViewModel {
        parametersOf(sectionId, studentId, competencyId)
    }
    val state: State<PeriodLevelsUiState> = viewModel.state.collectAsStateWithLifecycle()
    var message: String? by remember { mutableStateOf(null) }
    val messages: Map<PeriodLevelsMessage, String> = periodLevelsMessages()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is PeriodLevelsUiEffect.NavigateToWorkedCompetencies ->
                    onWorkedCompetencies(effect.sectionId, effect.periodId, effect.area)

                PeriodLevelsUiEffect.NavigateBack -> onBack()
                is PeriodLevelsUiEffect.ShowMessage -> message = messages.getValue(effect.message)
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

@Composable
private fun periodLevelsMessages(): Map<PeriodLevelsMessage, String> = mapOf(
    PeriodLevelsMessage.SAVE_FAILED to stringResource(R.string.period_levels_message_save_failed),
)
