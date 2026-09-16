package com.emm.gema.feature.students.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.theme.asDayMonthYear
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GEmptyState
import com.emm.gema.core.ui.GExtendedFab
import com.emm.gema.core.ui.GGroupHeader
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GSearchField
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.feature.students.R

@Composable
fun StudentsScreen(
    state: StudentsUiState,
    onIntent: (StudentsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    onMessageDismissed: () -> Unit = {},
) {
    val activeCountLabel: String = pluralStringResource(
        R.plurals.students_active_count,
        state.activeStudents.size,
        state.activeStudents.size,
    )
    val withdrawnCountLabel: String = pluralStringResource(
        R.plurals.students_withdrawn_count,
        state.withdrawnStudents.size,
        state.withdrawnStudents.size,
    )
    val listState: LazyListState = rememberLazyListState()
    GScreen(
        topBar = {
            GTopBar(
                title = "Alumnos · ${state.sectionTitle}",
                subtitle = "$activeCountLabel · $withdrawnCountLabel",
                onBackClick = { onIntent(StudentsUiIntent.BackClicked) },
                showHairline = listState.canScrollBackward,
                actions = {
                    GButton(
                        text = "Importar",
                        onClick = { onIntent(StudentsUiIntent.ImportClicked) },
                        variant = GButtonVariant.SECONDARY,
                        icon = Icons.Filled.Download,
                    )
                },
            )
        },
        fab = {
            GExtendedFab(
                text = "Agregar alumno",
                icon = Icons.Filled.Add,
                onClick = { onIntent(StudentsUiIntent.AddStudentClicked) },
            )
        },
        contentGutter = false,
        modifier = modifier,
    ) { padding: PaddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = GemaSpacing.small),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.extraSmall),
        ) {
            if (message != null) {
                item {
                    GBanner(
                        text = message,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = GemaSpacing.screenGutter),
                        tone = GBannerTone.ERROR,
                        actionText = "Entendido",
                        onActionClick = onMessageDismissed,
                    )
                }
            }
            item {
                GSearchField(
                    query = state.query,
                    onQueryChange = { onIntent(StudentsUiIntent.QueryChanged(it)) },
                    placeholder = "Buscar",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = GemaSpacing.screenGutter),
                )
            }
            if (state.isEmpty) {
                item {
                    GEmptyState(
                        title = "Todavía no hay alumnos",
                        message = "Agrega a tus alumnos con su código de estudiante " +
                            "y su nombre como aparece en SIAGIE.",
                        actionLabel = "Agregar alumno",
                        onActionClick = { onIntent(StudentsUiIntent.AddStudentClicked) },
                    )
                }
            }
            items(state.activeStudents, key = { it.id.value }) { row ->
                GListItem(
                    title = row.displayName,
                    modifier = Modifier.fillMaxWidth(),
                    subtitle = row.studentCode,
                    hasChevron = true,
                    onClick = { onIntent(StudentsUiIntent.StudentClicked(row.id)) },
                )
            }
            if (state.withdrawnStudents.isNotEmpty()) {
                item {
                    GGroupHeader(
                        title = "RETIRADOS",
                        count = state.withdrawnStudents.size,
                        isExpanded = state.isWithdrawnExpanded,
                        onClick = { onIntent(StudentsUiIntent.WithdrawnSectionToggled) },
                    )
                }
            }
            if (state.isWithdrawnExpanded) {
                items(state.withdrawnStudents, key = { it.id.value }) { row ->
                    WithdrawnStudentItem(row = row, onIntent = onIntent)
                }
            }
        }
    }
}

@Composable
private fun WithdrawnStudentItem(
    row: StudentRow,
    onIntent: (StudentsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    GListItem(
        title = row.displayName,
        modifier = modifier.fillMaxWidth(),
        subtitle = row.withdrawalDate?.let { "Retirado el ${it.asDayMonthYear()}" } ?: row.studentCode,
        onClick = { onIntent(StudentsUiIntent.StudentClicked(row.id)) },
        trailing = {
            GButton(
                text = "Reincorporar",
                onClick = { onIntent(StudentsUiIntent.ReactivateClicked(row.id)) },
                variant = GButtonVariant.TEXT,
            )
        },
    )
}

@PreviewLightDark
@Composable
private fun StudentsScreenPreview() {
    GemaTheme {
        StudentsScreen(
            state = StudentsUiState(
                isLoading = false,
                sectionTitle = "3ro A",
                activeStudents = listOf(
                    StudentRow(StudentId("1"), "ACOSTA RIVERA, Luz Maria", "12345678901234"),
                    StudentRow(StudentId("2"), "BAUTISTA QUISPE, Jose", "12345678901235"),
                ),
            ),
            onIntent = {},
        )
    }
}
