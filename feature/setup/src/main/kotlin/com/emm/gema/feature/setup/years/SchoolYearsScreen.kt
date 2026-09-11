package com.emm.gema.feature.setup.years

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
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GTopBar

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
        bottomAction = {
            GButton(
                text = "Nuevo año",
                onClick = { onIntent(SchoolYearsUiIntent.AddYearClicked) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) { padding: PaddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(GemaSpacing.screenGutter),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        ) {
            items(state.years, key = { it.id }) { row ->
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
    GListItem(
        title = if (row.isActive) "${row.label} · Activo" else row.label,
        modifier = modifier.fillMaxWidth(),
        subtitle = "${row.dateRangeLabel} · ${row.periodKindLabel} · ${row.sectionCount} secciones",
        onClick = { onIntent(SchoolYearsUiIntent.YearClicked(row.id)) },
        trailing = {
            GButton(
                text = "Periodos",
                onClick = { onIntent(SchoolYearsUiIntent.PeriodsClicked(row.id)) },
                variant = GButtonVariant.TEXT,
            )
        },
    )
}

@PreviewLightDark
@Composable
private fun SchoolYearsScreenPreview() {
    GemaTheme {
        SchoolYearsScreen(
            state = SchoolYearsUiState(
                isLoading = false,
                years = listOf(
                    SchoolYearRow("2026", "2026", "02/03/2026 - 18/12/2026", "Bimestre", 2, true),
                ),
            ),
            onIntent = {},
        )
    }
}
