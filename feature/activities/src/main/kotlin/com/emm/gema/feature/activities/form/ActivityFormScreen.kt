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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GCheckRow
import com.emm.gema.core.ui.GDateField
import com.emm.gema.core.ui.GDialog
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GTextField
import com.emm.gema.core.ui.GTopBar
import java.time.LocalDate

@Composable
fun ActivityFormScreen(
    state: ActivityFormUiState,
    onIntent: (ActivityFormUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
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
                .padding(padding)
                .padding(horizontal = GemaSpacing.screenGutter),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.medium),
        ) {
            if (message != null) {
                item { Text(text = message, style = MaterialTheme.typography.bodyMedium) }
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
                        errorText = state.dateError,
                    )
                    if (state.resolvedPeriodLabel != null) {
                        Text(
                            text = "Cae en el ${state.resolvedPeriodLabel}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (state.periodChangeWarning != null) {
                        Text(
                            text = state.periodChangeWarning,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            item {
                Text(text = "Competencias trabajadas", style = MaterialTheme.typography.titleSmall)
            }
            state.competencyGroups.forEach { group ->
                item {
                    Text(
                        text = group.areaName.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(group.competencies, key = { it.id }) { competency ->
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
            Text(
                text = "Se perderán las evidencias registradas para esta actividad.",
                style = MaterialTheme.typography.bodyMedium,
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
                            CompetencyToggleRow("PPSS-1", 1, "Construye su identidad"),
                            CompetencyToggleRow("PPSS-2", 2, "Convive y participa democráticamente"),
                        ),
                    ),
                ),
                selectedCompetencyIds = setOf("PPSS-2"),
            ),
            onIntent = {},
        )
    }
}
