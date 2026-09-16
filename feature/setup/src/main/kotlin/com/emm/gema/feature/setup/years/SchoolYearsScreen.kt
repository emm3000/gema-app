package com.emm.gema.feature.setup.years

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.emm.gema.core.theme.numericRangeLabel
import com.emm.gema.core.ui.GBadge
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GExtendedFab
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.feature.setup.R
import com.emm.gema.feature.setup.periodCountPlural
import java.time.LocalDate

@Composable
fun SchoolYearsScreen(
    state: SchoolYearsUiState,
    onIntent: (SchoolYearsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState: LazyListState = rememberLazyListState()
    GScreen(
        topBar = {
            GTopBar(
                title = stringResource(R.string.school_years_title),
                subtitle = stringResource(R.string.school_years_subtitle),
                onBackClick = { onIntent(SchoolYearsUiIntent.BackClicked) },
                isContentScrolled = listState.canScrollBackward,
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
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = GemaSpacing.screenGutter),
        ) {
            itemsIndexed(state.years, key = { _, row -> row.id.value }) { index, row ->
                SchoolYearItem(
                    row = row,
                    onIntent = onIntent,
                    showDivider = index != state.years.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun SchoolYearItem(
    row: SchoolYearRow,
    onIntent: (SchoolYearsUiIntent) -> Unit,
    showDivider: Boolean,
    modifier: Modifier = Modifier,
) {
    val periodCountLabel: String = pluralStringResource(
        row.periodKind.periodCountPlural(),
        row.periodKind.periodCount,
        row.periodKind.periodCount,
    )
    val sectionCountLabel: String = pluralStringResource(
        R.plurals.school_years_section_count,
        row.sectionCount,
        row.sectionCount,
    )
    val subtitle: String = "${numericRangeLabel(row.startDate, row.endDate)} · $periodCountLabel\n$sectionCountLabel"
    GListItem(
        title = row.label,
        modifier = modifier.fillMaxWidth(),
        titleStyle = GTextStyle.NUMERAL,
        titleTrailing = if (row.isActive) {
            { GBadge(text = stringResource(R.string.school_years_badge_active)) }
        } else {
            null
        },
        subtitle = subtitle,
        subtitleStyle = GTextStyle.BODY_LARGE,
        onClick = { onIntent(SchoolYearsUiIntent.YearClicked(row.id)) },
        showDivider = showDivider,
        trailing = {
            GButton(
                text = stringResource(R.string.school_years_periods_label),
                onClick = { onIntent(SchoolYearsUiIntent.PeriodsClicked(row.id)) },
                variant = GButtonVariant.TEXT,
                trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
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
