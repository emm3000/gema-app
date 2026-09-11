package com.emm.gema.feature.students.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GEmptyState
import com.emm.gema.core.ui.GExtendedFab
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GSearchField
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.feature.students.asDayMonthYear

@Composable
fun StudentsScreen(
    state: StudentsUiState,
    onIntent: (StudentsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    onMessageDismissed: () -> Unit = {},
) {
    GScreen(
        topBar = {
            GTopBar(
                title = "Alumnos · ${state.sectionTitle}",
                subtitle = "${state.activeStudents.size} activos · ${state.withdrawnStudents.size} retirados",
                onBackClick = { onIntent(StudentsUiIntent.BackClicked) },
                actions = {
                    GButton(
                        text = "Importar",
                        onClick = { onIntent(StudentsUiIntent.ImportClicked) },
                        variant = GButtonVariant.SECONDARY,
                    )
                },
            )
        },
        floatingAction = {
            GExtendedFab(
                text = "Agregar alumno",
                onClick = { onIntent(StudentsUiIntent.AddStudentClicked) },
            )
        },
        modifier = modifier,
    ) { padding: PaddingValues ->
        LazyColumn(
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
                        modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.fillMaxWidth(),
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
                    WithdrawnSectionHeader(
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
private fun WithdrawnSectionHeader(
    count: Int,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GListItem(
        title = "RETIRADOS ($count)",
        modifier = modifier.fillMaxWidth(),
        titleStyle = GTextStyle.LABEL_SMALL_EMPHASIS,
        isEmphasized = true,
        isExpanded = isExpanded,
        showDivider = false,
        onClick = onClick,
    )
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
                sectionTitle = "3° A",
                activeStudents = listOf(
                    StudentRow(StudentId("1"), "ACOSTA RIVERA, Luz Maria", "12345678901234"),
                    StudentRow(StudentId("2"), "BAUTISTA QUISPE, Jose", "12345678901235"),
                ),
            ),
            onIntent = {},
        )
    }
}
