package com.emm.gema.feature.activities.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GCheckRow
import com.emm.gema.core.ui.GDateField
import com.emm.gema.core.ui.GDialog
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextField
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.feature.activities.R
import java.time.LocalDate

@Composable
fun ActivityFormScreen(
    state: ActivityFormUiState,
    onIntent: (ActivityFormUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    dateErrorText: String? = null,
) {
    GScreen(
        topBar = {
            GTopBar(
                title = if (state.activityId == null) "Nueva actividad" else "Editar actividad",
                onBackClick = { onIntent(ActivityFormUiIntent.BackClicked) },
            )
        },
        modifier = modifier,
        bottomAction = {
            GButton(
                text = "Guardar",
                onClick = { onIntent(ActivityFormUiIntent.SaveClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canSave,
            )
        },
    ) { padding: PaddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.medium),
        ) {
            if (message != null) {
                item { GText(text = message, style = GTextStyle.BODY_MEDIUM) }
            }
            item {
                GTextField(
                    value = state.name,
                    onValueChange = { onIntent(ActivityFormUiIntent.NameChanged(it)) },
                    label = "Nombre",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Column {
                    GDateField(
                        value = state.date,
                        onValueChange = { onIntent(ActivityFormUiIntent.DateChanged(it)) },
                        label = "Fecha",
                        modifier = Modifier.fillMaxWidth(),
                        errorText = dateErrorText,
                    )
                    if (state.resolvedPeriodLabel != null) {
                        GText(
                            text = "Cae en el ${state.resolvedPeriodLabel}.",
                            style = GTextStyle.BODY_SMALL,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (state.hasPeriodChangeWarning) {
                        GText(
                            text = stringResource(R.string.activity_form_period_change_warning),
                            style = GTextStyle.BODY_SMALL,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            item {
                GText(text = "Competencias trabajadas", style = GTextStyle.TITLE_SMALL)
            }
            state.competencyGroups.forEach { group ->
                item {
                    GText(
                        text = group.areaName.uppercase(),
                        style = GTextStyle.LABEL_MEDIUM,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(group.competencies, key = { it.id.value }) { competency ->
                    GCheckRow(
                        title = competency.name,
                        isChecked = competency.id in state.selectedCompetencyIds,
                        onCheckedChange = {
                            onIntent(ActivityFormUiIntent.CompetencyToggled(competency.id, it))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        prefix = competency.siagieOrdinal.toString().padStart(2, '0'),
                    )
                }
            }
            if (state.canDelete) {
                item {
                    GButton(
                        text = "Eliminar actividad",
                        onClick = { onIntent(ActivityFormUiIntent.DeleteClicked) },
                        variant = GButtonVariant.DESTRUCTIVE,
                    )
                }
            }
        }
    }

    if (state.isDeleteConfirmVisible) {
        GDialog(
            title = "¿Eliminar la actividad?",
            confirmText = "Eliminar",
            onConfirm = { onIntent(ActivityFormUiIntent.DeleteConfirmed) },
            onDismiss = { onIntent(ActivityFormUiIntent.DeleteDismissed) },
            dismissText = "Cancelar",
            isDestructive = true,
        ) {
            GText(
                text = "Se perderán las evidencias registradas para esta actividad.",
                style = GTextStyle.BODY_MEDIUM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ActivityFormScreenPreview() {
    GemaTheme {
        ActivityFormScreen(
            state = ActivityFormUiState(
                isLoading = false,
                name = "Debate del aula",
                date = LocalDate.of(2026, 6, 22),
                resolvedPeriodLabel = "II Bimestre",
                competencyGroups = listOf(
                    CompetencyGroup(
                        areaName = "Personal Social",
                        competencies = listOf(
                            CompetencyToggleRow(CompetencyId("PPSS-1"), 1, "Construye su identidad"),
                            CompetencyToggleRow(CompetencyId("PPSS-2"), 2, "Convive y participa democráticamente"),
                        ),
                    ),
                ),
                selectedCompetencyIds = setOf(CompetencyId("PPSS-2")),
            ),
            onIntent = {},
        )
    }
}
