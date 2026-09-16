package com.emm.gema.feature.students.form

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GDateField
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GSegmentOption
import com.emm.gema.core.ui.GSegmentedPicker
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextField
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.core.ui.GValidatedHelperText
import com.emm.gema.feature.students.R
import java.time.LocalDate

@Composable
fun StudentFormScreen(
    state: StudentFormUiState,
    onIntent: (StudentFormUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    onMessageDismissed: () -> Unit = {},
) {
    val scrollState: ScrollState = rememberScrollState()
    GScreen(
        topBar = {
            GTopBar(
                title = if (state.studentId == null) "Nuevo alumno" else "Editar alumno",
                subtitle = state.sectionTitle.ifBlank { null },
                onBackClick = { onIntent(StudentFormUiIntent.BackClicked) },
                isContentScrolled = scrollState.value > 0,
            )
        },
        modifier = modifier,
        bottomAction = {
            GButton(
                text = "Guardar",
                onClick = { onIntent(StudentFormUiIntent.SaveClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canSave,
            )
        },
    ) { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(vertical = GemaSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.medium),
        ) {
            if (message != null) {
                GBanner(
                    text = message,
                    modifier = Modifier.fillMaxWidth(),
                    tone = GBannerTone.ERROR,
                    actionText = "Entendido",
                    onActionClick = onMessageDismissed,
                )
            }
            val studentCodeErrorText: String? = state.studentCodeError.asText()
            GTextField(
                value = state.studentCode,
                onValueChange = { onIntent(StudentFormUiIntent.StudentCodeChanged(it)) },
                label = "Código del estudiante",
                modifier = Modifier.fillMaxWidth(),
                errorText = studentCodeErrorText,
                keyboardType = KeyboardType.Number,
            )
            if (studentCodeErrorText == null) {
                GValidatedHelperText(
                    text = state.studentCodeHint,
                    isValid = state.isStudentCodeValid,
                )
            }
            GTextField(
                value = state.fullName,
                onValueChange = { onIntent(StudentFormUiIntent.FullNameChanged(it)) },
                label = "Apellidos y nombres",
                modifier = Modifier.fillMaxWidth(),
                supportingText = "Apellidos primero, como en SIAGIE.",
                errorText = state.fullNameError.asText(),
            )
            if (state.hasSiagieId) {
                GBanner(
                    text = stringResource(R.string.student_form_siagie_origin_note),
                    modifier = Modifier.fillMaxWidth(),
                    tone = GBannerTone.INFO,
                    icon = Icons.Filled.Description,
                )
            }
            GText(
                text = stringResource(R.string.student_form_status_label),
                style = GTextStyle.LABEL_SMALL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { heading() },
            )
            GSegmentedPicker(
                options = listOf(
                    GSegmentOption(
                        value = false,
                        label = "Activo",
                        contentDescription = stringResource(R.string.student_form_status_active),
                    ),
                    GSegmentOption(
                        value = true,
                        label = "Retirado",
                        contentDescription = stringResource(R.string.student_form_status_withdrawn),
                    ),
                ),
                selected = state.isWithdrawn,
                onSelect = { onIntent(StudentFormUiIntent.WithdrawnToggled(it ?: state.isWithdrawn)) },
                modifier = Modifier.fillMaxWidth(),
                activeContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                activeContentColor = MaterialTheme.colorScheme.onSurface,
            )
            if (state.isWithdrawn) {
                GDateField(
                    value = state.withdrawalDate,
                    onValueChange = { onIntent(StudentFormUiIntent.WithdrawalDateChanged(it)) },
                    label = "Fecha de retiro",
                    modifier = Modifier.fillMaxWidth(),
                    errorText = state.withdrawalDateError.asText(),
                )
                GText(
                    text = stringResource(R.string.student_form_withdrawal_helper),
                    style = GTextStyle.BODY_SMALL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
        }
    }
}

@Composable
private fun StudentCodeError?.asText(): String? = when (this) {
    null -> null
    is StudentCodeError.InvalidLength -> stringResource(R.string.student_form_error_invalid_code, length)
    StudentCodeError.DuplicateCode -> stringResource(R.string.student_form_error_duplicate_code)
}

@Composable
private fun FullNameError?.asText(): String? = when (this) {
    null -> null
    FullNameError.BLANK -> stringResource(R.string.student_form_error_missing_name)
}

@Composable
private fun WithdrawalDateError?.asText(): String? = when (this) {
    null -> null
    WithdrawalDateError.MISSING -> stringResource(R.string.student_form_error_missing_withdrawal_date)
}

@PreviewLightDark
@Composable
private fun StudentFormScreenPreview() {
    GemaTheme {
        StudentFormScreen(
            state = StudentFormUiState(
                isLoading = false,
                studentId = StudentId("student-1"),
                studentCode = "12345678901234",
                studentCodeHint = "14 de 14 dígitos",
                fullName = "ACOSTA RIVERA, Luz Maria",
                isWithdrawn = true,
                withdrawalDate = LocalDate.of(2026, 9, 4),
                canSave = true,
                hasSiagieId = true,
                sectionTitle = "3ro A",
                isStudentCodeValid = true,
            ),
            onIntent = {},
        )
    }
}
