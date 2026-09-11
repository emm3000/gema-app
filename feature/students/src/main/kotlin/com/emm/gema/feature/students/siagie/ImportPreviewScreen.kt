package com.emm.gema.feature.students.siagie

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GBorderedContainer
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GCheckRow
import com.emm.gema.core.ui.GExpandableGroupRow
import com.emm.gema.core.ui.GFileCard
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTintedGroupContent
import com.emm.gema.core.ui.GTopBar

private const val NOTHING_IS_LOST: String =
    "Nada se borra. Los retirados conservan su asistencia y sus niveles."

private const val NOTHING_IS_WRITTEN: String = "Nada se escribe hasta que apliques"

@Composable
fun ImportPreviewScreen(
    state: ImportPreviewUiState,
    onIntent: (ImportPreviewUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
) {
    GScreen(
        topBar = {
            GTopBar(
                title = "Importar de SIAGIE",
                subtitle = NOTHING_IS_WRITTEN,
                onBackClick = { onIntent(ImportPreviewUiIntent.BackClicked) },
            )
        },
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        bottomAction = { ImportActions(state = state, onIntent = onIntent) },
    ) { padding: PaddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = GemaSpacing.small),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.extraSmall),
        ) {
            if (state.rejection != null) {
                rejection(state.rejection)
            } else {
                plan(state = state, onIntent = onIntent)
            }
        }
    }
}

private fun LazyListScope.rejection(rejection: ImportRejection) {
    item {
        GBanner(
            text = rejection.reason,
            modifier = Modifier.fillMaxWidth(),
            tone = GBannerTone.ERROR,
        )
    }
    if (rejection.expected != null) {
        item { GListItem(title = "Sección seleccionada", trailingText = rejection.expected) }
    }
    if (rejection.found != null) {
        item { GListItem(title = "Archivo", trailingText = rejection.found) }
    }
    item {
        GBanner(
            text = "Elige otro archivo o abre la sección correcta.",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun LazyListScope.plan(state: ImportPreviewUiState, onIntent: (ImportPreviewUiIntent) -> Unit) {
    item {
        GFileCard(
            title = state.fileName,
            subtitle = "${state.sectionTitle} · ${state.rosterSize} alumnos en el archivo",
        )
    }
    item {
        GBorderedContainer {
            group(
                title = "Se crearán",
                group = ImportGroup.CREATED,
                rows = state.created,
                state = state,
                onIntent = onIntent,
            )
            group(
                title = "Se actualizarán",
                group = ImportGroup.UPDATED,
                rows = state.updated,
                state = state,
                onIntent = onIntent,
            )
            withdrawals(state = state, onIntent = onIntent)
        }
    }
    item {
        GText(
            text = NOTHING_IS_LOST,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GemaSpacing.medium),
            style = GTextStyle.BODY_SMALL,
        )
    }
}

@Composable
private fun ColumnScope.group(
    title: String,
    group: ImportGroup,
    rows: List<ImportStudentRow>,
    state: ImportPreviewUiState,
    onIntent: (ImportPreviewUiIntent) -> Unit,
) {
    val isExpanded: Boolean = state.expandedGroup == group
    GExpandableGroupRow(
        title = title,
        count = rows.size,
        isExpanded = isExpanded,
        onClick = { onIntent(ImportPreviewUiIntent.GroupToggled(group)) },
    )
    if (isExpanded) {
        GTintedGroupContent {
            rows.forEach { row: ImportStudentRow ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = GemaSpacing.medium, vertical = GemaSpacing.small),
                ) {
                    GText(text = row.displayName, style = GTextStyle.BODY_LARGE)
                    GText(
                        text = row.studentCode,
                        style = GTextStyle.BODY_SMALL,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.withdrawals(state: ImportPreviewUiState, onIntent: (ImportPreviewUiIntent) -> Unit) {
    val isExpanded: Boolean = state.expandedGroup == ImportGroup.WITHDRAWN
    GExpandableGroupRow(
        title = "Se propondrán como retirados",
        count = state.proposedWithdrawals.size,
        isExpanded = isExpanded,
        showDivider = false,
        onClick = { onIntent(ImportPreviewUiIntent.GroupToggled(ImportGroup.WITHDRAWN)) },
    )
    if (isExpanded) {
        GTintedGroupContent {
            state.proposedWithdrawals.forEach { row: ImportWithdrawalRow ->
                GCheckRow(
                    title = row.displayName,
                    isChecked = row.isSelected,
                    onCheckedChange = { isSelected: Boolean ->
                        onIntent(ImportPreviewUiIntent.WithdrawalToggled(row.studentId, isSelected))
                    },
                )
            }
        }
    }
}

@Composable
private fun ImportActions(
    state: ImportPreviewUiState,
    onIntent: (ImportPreviewUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = GemaSpacing.medium),
        horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
    ) {
        GButton(
            text = "Cancelar",
            onClick = { onIntent(ImportPreviewUiIntent.CancelClicked) },
            modifier = Modifier.weight(1f),
            variant = GButtonVariant.SECONDARY,
        )
        GButton(
            text = "Aplicar",
            onClick = { onIntent(ImportPreviewUiIntent.ApplyClicked) },
            modifier = Modifier.weight(1f),
            enabled = state.canApply,
            isBusy = state.isApplying,
        )
    }
}

@PreviewLightDark
@Composable
private fun ImportPreviewScreenPreview() {
    GemaTheme {
        ImportPreviewScreen(
            state = ImportPreviewUiState(
                isLoading = false,
                fileName = "6 Primaria EBR.xlsx",
                sectionTitle = "6° A",
                rosterSize = 30,
                created = listOf(ImportStudentRow("10000000000001", "ALVARADO QUISPE, MARIA")),
                updated = listOf(ImportStudentRow("10000000000002", "BAUTISTA HUAMAN, JOSE")),
                proposedWithdrawals = listOf(ImportWithdrawalRow(StudentId("student-3"), "TORRES PINO, LUIS", true)),
            ),
            onIntent = {},
        )
    }
}
