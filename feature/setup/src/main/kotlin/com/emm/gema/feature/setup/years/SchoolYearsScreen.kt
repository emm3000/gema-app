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
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GExtendedFab
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.core.ui.GYearCard
import com.emm.gema.feature.setup.R
import com.emm.gema.feature.setup.numericRangeLabel
import java.time.LocalDate

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
    val periodCountLabel: String = pluralStringResource(
        row.periodKind.periodCountPlural(),
        row.periodKind.periodCount,
        row.periodKind.periodCount,
    )
    GYearCard(
        label = row.label,
        subtitle = "${numericRangeLabel(row.startDate, row.endDate)} · $periodCountLabel",
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

private fun PeriodKind.periodCountPlural(): Int = when (this) {
    PeriodKind.BIMESTER -> R.plurals.setup_periods_bimester_count
    PeriodKind.TRIMESTER -> R.plurals.setup_periods_trimester_count
}

@PreviewLightDark
@Composable
private fun SchoolYearsScreenPreview() {
    GemaTheme {
        SchoolYearsScreen(
            state = SchoolYearsUiState(
                isLoading = false,
                years = listOf(
                    SchoolYearRow(
                        id = SchoolYearId("2026"),
                        label = "2026",
                        startDate = LocalDate.of(2026, 3, 2),
                        endDate = LocalDate.of(2026, 12, 18),
                        periodKind = PeriodKind.BIMESTER,
                        sectionCount = 2,
                        isActive = true,
                    ),
                    SchoolYearRow(
                        id = SchoolYearId("2025"),
                        label = "2025",
                        startDate = LocalDate.of(2025, 3, 1),
                        endDate = LocalDate.of(2025, 12, 19),
                        periodKind = PeriodKind.TRIMESTER,
                        sectionCount = 1,
                        isActive = false,
                    ),
                ),
            ),
            onIntent = {},
        )
    }
}
