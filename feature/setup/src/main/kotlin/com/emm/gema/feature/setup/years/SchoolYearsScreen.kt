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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GExtendedFab
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.core.ui.GYearCard
import com.emm.gema.feature.setup.R

@Composable
fun SchoolYearsScreen(
    state: SchoolYearsUiState,
    onIntent: (SchoolYearsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    GScreen(
        topBar = {
            GTopBar(
                title = stringResource(R.string.school_years_title),
                subtitle = stringResource(R.string.school_years_subtitle),
                onBackClick = { onIntent(SchoolYearsUiIntent.BackClicked) },
            )
        },
        modifier = modifier,
        fab = {
            GExtendedFab(
                text = stringResource(R.string.school_years_add_year),
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
        sectionCountLabel = pluralStringResource(
            R.plurals.school_years_section_count,
            row.sectionCount,
            row.sectionCount,
        ),
        periodsLabel = stringResource(R.string.school_years_periods_label),
        isActive = row.isActive,
        badgeText = if (row.isActive) stringResource(R.string.school_years_badge_active) else null,
        onClick = { onIntent(SchoolYearsUiIntent.YearClicked(row.id)) },
        onPeriodsClick = { onIntent(SchoolYearsUiIntent.PeriodsClicked(row.id)) },
        modifier = modifier.fillMaxWidth(),
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
                    SchoolYearRow(SchoolYearId("2026"), "2026", "02/03/2026 - 18/12/2026", "Bimestre", 2, true),
                    SchoolYearRow(SchoolYearId("2025"), "2025", "01/03/2025 - 19/12/2025", "Trimestre", 1, false),
                ),
            ),
            onIntent = {},
        )
    }
}
