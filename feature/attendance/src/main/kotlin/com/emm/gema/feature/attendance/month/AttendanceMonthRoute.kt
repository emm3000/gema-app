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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.ui.share.shareFile
import com.emm.gema.feature.attendance.R
import java.io.File
import java.time.YearMonth
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AttendanceMonthRoute(
    sectionId: SectionId,
    month: YearMonth?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AttendanceMonthViewModel = koinViewModel { parametersOf(sectionId, month) }
    val state: State<AttendanceMonthUiState> = viewModel.state.collectAsStateWithLifecycle()
    val context: Context = LocalContext.current
    var message: String? by remember { mutableStateOf(null) }
    val messages: Map<AttendanceMonthMessage, String> = attendanceMonthMessages()
    val chooserTitle: String = stringResource(R.string.attendance_month_chooser_title)

    val pickTemplate = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) viewModel.onIntent(AttendanceMonthUiIntent.TemplatePicked(uri.toString()))
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is AttendanceMonthUiEffect.OpenDocumentPicker -> pickTemplate.launch(effect.mimeTypes.toTypedArray())
                is AttendanceMonthUiEffect.ShareFile ->
                    context.shareFile(File(effect.path), effect.mimeType, chooserTitle)
                is AttendanceMonthUiEffect.ShowMessage -> message = messages.getValue(effect.message)
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

@Composable
private fun attendanceMonthMessages(): Map<AttendanceMonthMessage, String> = mapOf(
    AttendanceMonthMessage.EXPORT_FAILED to stringResource(R.string.attendance_month_message_export_failed),
)
