package com.emm.gema.feature.setup.years

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GExtendedFab
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.core.ui.GYearCard

@Composable
fun SchoolYearsScreen(
    state: SchoolYearsUiState,
    onIntent: (SchoolYearsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    GScreen(
        topBar = {
            GTopBar(
                title = "Años escolares",
                onBackClick = { onIntent(SchoolYearsUiIntent.BackClicked) },
            )
        },
        modifier = modifier,
        fab = {
            GExtendedFab(
                text = "Nuevo año",
                icon = Icons.Filled.Add,
                onClick = { onIntent(SchoolYearsUiIntent.AddYearClicked) },
            )
        },
    ) { padding: PaddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = GemaSpacing.screenGutter),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        ) {
            items(state.years, key = { it.id.value }) { row ->
                SchoolYearItem(row = row, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun SchoolYearItem(
    row: SchoolYearRow,
    onIntent: (SchoolYearsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    GYearCard(
        label = row.label,
        subtitle = "${row.dateRangeLabel} · ${row.periodKindLabel}",
        sectionCountLabel = sectionCountLabel(row.sectionCount),
        periodsLabel = "Periodos",
        isActive = row.isActive,
        onClick = { onIntent(SchoolYearsUiIntent.YearClicked(row.id)) },
        onPeriodsClick = { onIntent(SchoolYearsUiIntent.PeriodsClicked(row.id)) },
        modifier = modifier.fillMaxWidth(),
    )
}

private fun sectionCountLabel(sectionCount: Int): String = if (sectionCount == 1) {
    "1 sección"
} else {
    "$sectionCount secciones"
}

@PreviewLightDark
@Composable
private fun SchoolYearsScreenPreview() {
    GemaTheme {
        SchoolYearsScreen(
            state = SchoolYearsUiState(
                isLoading = false,
                years = listOf(
                    SchoolYearRow(SchoolYearId("2026"), "2026", "02/03/2026 - 18/12/2026", "Bimestre", 2, true),
                    SchoolYearRow(SchoolYearId("2025"), "2025", "01/03/2025 - 19/12/2025", "Trimestre", 1, false),
                ),
            ),
            onIntent = {},
        )
    }
}
