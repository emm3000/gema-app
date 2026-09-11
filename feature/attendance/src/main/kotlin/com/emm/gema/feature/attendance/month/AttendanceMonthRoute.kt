package com.emm.gema.feature.attendance.month

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.YearMonth

private const val CHOOSER_TITLE: String = "Compartir asistencia"

@Composable
fun AttendanceMonthRoute(
    sectionId: String,
    month: YearMonth?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AttendanceMonthViewModel = koinViewModel { parametersOf(sectionId, month) }
    val state: State<AttendanceMonthUiState> = viewModel.state.collectAsStateWithLifecycle()
    val context: Context = LocalContext.current
    var message: String? by remember { mutableStateOf(null) }

    val pickTemplate = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) viewModel.onIntent(AttendanceMonthUiIntent.TemplatePicked(uri.toString()))
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is AttendanceMonthUiEffect.OpenDocumentPicker -> pickTemplate.launch(effect.mimeTypes.toTypedArray())
                is AttendanceMonthUiEffect.ShareFile ->
                    context.shareAttendanceExport(effect.path, effect.mimeType, CHOOSER_TITLE)
                is AttendanceMonthUiEffect.ShowMessage -> message = effect.text
                AttendanceMonthUiEffect.NavigateBack -> onBack()
            }
        }
    }

    AttendanceMonthScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        message = message,
        onMessageDismissed = { message = null },
    )
}
