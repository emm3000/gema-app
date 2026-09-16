package com.emm.gema.feature.students.siagie

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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
import com.emm.gema.core.ui.GFileCard
import com.emm.gema.core.ui.GIcon
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTintedGroupContent
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.feature.students.R

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
    val listState: LazyListState = rememberLazyListState()
    GScreen(
        topBar = {
            GTopBar(
                title = "Importar de SIAGIE",
                subtitle = NOTHING_IS_WRITTEN,
                onBackClick = { onIntent(ImportPreviewUiIntent.BackClicked) },
                isContentScrolled = listState.canScrollBackward,
            )
        },
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        bottomAction = { ImportActions(state = state, onIntent = onIntent) },
    ) { padding: PaddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = GemaSpacing.small),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.extraSmall),
        ) {
            if (state.rejection != null) {
                rejection(fileName = state.fileName, rejection = state.rejection)
            } else {
                plan(state = state, onIntent = onIntent)
            }
        }
    }
}

private fun LazyListScope.rejection(fileName: String, rejection: ImportRejection) {
    item { GFileCard(title = fileName) }
    item {
        GBanner(
            text = rejection.reason,
            modifier = Modifier.fillMaxWidth(),
            tone = GBannerTone.ERROR,
            hasLeadingDot = true,
        )
    }
    if (rejection.expected != null) {
        item { GListItem(title = "Sección abierta", trailingText = rejection.expected) }
    }
    if (rejection.found != null) {
        item {
            GListItem(
                title = rejection.foundLabel.orEmpty(),
                trailingText = rejection.found,
                trailingTextColor = MaterialTheme.colorScheme.error,
            )
        }
    }
    item {
        GText(
            text = rejection.instruction,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GemaSpacing.medium),
            style = GTextStyle.BODY_LARGE,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    val expandedStateDescription: String = expandedStateDescription(isExpanded)
    GListItem(
        title = title,
        modifier = Modifier.semantics { stateDescription = expandedStateDescription },
        onClick = { onIntent(ImportPreviewUiIntent.GroupToggled(group)) },
        trailing = { GroupExpandTrailing(count = rows.size, isExpanded = isExpanded) },
    )
    if (isExpanded) {
        GTintedGroupContent {
            rows.forEach { row: ImportStudentRow ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = GemaSpacing.minimumTouchTarget)
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
    val expandedStateDescription: String = expandedStateDescription(isExpanded)
    GListItem(
        title = "Se propondrán como retirados",
        modifier = Modifier.semantics { stateDescription = expandedStateDescription },
        onClick = { onIntent(ImportPreviewUiIntent.GroupToggled(ImportGroup.WITHDRAWN)) },
        trailing = { GroupExpandTrailing(count = state.proposedWithdrawals.size, isExpanded = isExpanded) },
        showDivider = isExpanded,
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
            GText(
                text = stringResource(R.string.import_preview_withdrawals_helper),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = GemaSpacing.medium, vertical = GemaSpacing.small),
                style = GTextStyle.BODY_SMALL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GroupExpandTrailing(count: Int, isExpanded: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GText(text = count.toString(), style = GTextStyle.NUMERAL)
        GIcon(icon = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown)
    }
}

@Composable
private fun expandedStateDescription(isExpanded: Boolean): String = stringResource(
    if (isExpanded) R.string.import_preview_group_expanded else R.string.import_preview_group_collapsed,
)

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
        if (state.rejection != null) {
            GButton(
                text = "Volver a alumnos",
                onClick = { onIntent(ImportPreviewUiIntent.CancelClicked) },
                modifier = Modifier.weight(1f),
                variant = GButtonVariant.SECONDARY,
            )
        } else {
            GButton(
                text = "Cancelar",
                onClick = { onIntent(ImportPreviewUiIntent.CancelClicked) },
                variant = GButtonVariant.SECONDARY,
            )
            GButton(
                text = "Aplicar importación",
                onClick = { onIntent(ImportPreviewUiIntent.ApplyClicked) },
                modifier = Modifier.weight(1f),
                enabled = state.canApply,
                isBusy = state.isApplying,
            )
        }
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
                sectionTitle = "6to A",
                rosterSize = 30,
                created = listOf(ImportStudentRow("10000000000001", "ALVARADO QUISPE, MARIA")),
                updated = listOf(ImportStudentRow("10000000000002", "BAUTISTA HUAMAN, JOSE")),
                proposedWithdrawals = listOf(ImportWithdrawalRow(StudentId("student-3"), "TORRES PINO, LUIS", true)),
            ),
            onIntent = {},
        )
    }
}
