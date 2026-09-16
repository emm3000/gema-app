package com.emm.gema.feature.activities.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.sectionTitle
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
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
private fun activityFormSubtitle(state: ActivityFormUiState): String? {
    val grade: Grade = state.grade ?: return null
    val sectionTitle: String = sectionTitle(grade, state.sectionName)
    return if (state.resolvedPeriodLabel != null) {
        stringResource(R.string.activity_form_subtitle, sectionTitle, state.resolvedPeriodLabel)
    } else {
        sectionTitle
    }
}

@Composable
fun ActivityFormScreen(
    state: ActivityFormUiState,
    onIntent: (ActivityFormUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    dateErrorText: String? = null,
) {
    val listState: LazyListState = rememberLazyListState()
    GScreen(
        topBar = {
            GTopBar(
                title = if (state.activityId == null) {
                    stringResource(R.string.activity_form_title_new)
                } else {
                    stringResource(R.string.activity_form_title_edit)
                },
                subtitle = activityFormSubtitle(state),
                onBackClick = { onIntent(ActivityFormUiIntent.BackClicked) },
                isContentScrolled = listState.canScrollBackward,
            )
        },
        modifier = modifier,
        contentGutter = false,
        bottomAction = {
            GButton(
                text = "Guardar",
                onClick = { onIntent(ActivityFormUiIntent.SaveClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canSave,
            )
        },
    ) { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                if (message != null) {
                    item {
                        GText(
                            text = message,
                            style = GTextStyle.BODY_LARGE,
                            modifier = Modifier.padding(horizontal = GemaSpacing.screenGutter),
                        )
                    }
                }
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = GemaSpacing.screenGutter),
                        verticalArrangement = Arrangement.spacedBy(GemaSpacing.medium),
                    ) {
                        GTextField(
                            value = state.name,
                            onValueChange = { onIntent(ActivityFormUiIntent.NameChanged(it)) },
                            label = "Nombre",
                            modifier = Modifier.fillMaxWidth(),
                        )
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
                                    text = stringResource(
                                        R.string.activity_form_resolved_period,
                                        state.resolvedPeriodLabel,
                                    ),
                                    style = GTextStyle.BODY_SMALL,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = GemaSpacing.small),
                                )
                            }
                            if (state.periodChangeFromLabel != null && state.resolvedPeriodLabel != null) {
                                GBanner(
                                    text = stringResource(
                                        R.string.activity_form_period_change_warning,
                                        state.periodChangeFromLabel,
                                        state.resolvedPeriodLabel,
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = GemaSpacing.rowGap)
                                        .semantics { liveRegion = LiveRegionMode.Polite },
                                    tone = GBannerTone.WARNING,
                                )
                            }
                        }
                        GText(
                            text = stringResource(R.string.activity_form_competencies_label).uppercase(),
                            style = GTextStyle.LABEL_SMALL,
                        )
                    }
                }
                state.competencyGroups.forEach { group ->
                    item {
                        GText(
                            text = group.areaName,
                            style = GTextStyle.BODY_SMALL_EMPHASIS,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = GemaSpacing.screenGutter),
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
                        DangerZone(onIntent = onIntent)
                    }
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
                style = GTextStyle.BODY_LARGE,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DangerZone(onIntent: (ActivityFormUiIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = GemaSpacing.screenGutter)
            .padding(top = GemaSpacing.extraLarge, bottom = GemaSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(GemaSpacing.small),
    ) {
        GText(
            text = stringResource(R.string.activity_form_delete_eyebrow),
            style = GTextStyle.LABEL_SMALL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GButton(
            text = stringResource(R.string.activity_form_delete_button),
            onClick = { onIntent(ActivityFormUiIntent.DeleteClicked) },
            variant = GButtonVariant.DESTRUCTIVE,
            modifier = Modifier.fillMaxWidth(),
        )
        GText(
            text = stringResource(R.string.activity_form_delete_helper),
            style = GTextStyle.BODY_SMALL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@PreviewLightDark
@Composable
private fun ActivityFormScreenPreview() {
    GemaTheme {
        ActivityFormScreen(
            state = ActivityFormUiState(
                isLoading = false,
                grade = Grade.THIRD,
                sectionName = "A",
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
