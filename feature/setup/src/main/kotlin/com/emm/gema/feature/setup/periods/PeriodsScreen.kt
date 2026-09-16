package com.emm.gema.feature.setup.periods

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBadge
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GDateField
import com.emm.gema.core.ui.GDivider
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.feature.setup.R
import com.emm.gema.feature.setup.periodCountPlural
import java.time.LocalDate

@Composable
fun PeriodsScreen(
    state: PeriodsUiState,
    onIntent: (PeriodsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    onMessageDismissed: () -> Unit = {},
    overlapErrorText: String? = null,
) {
    val periodKindCountLabel: String = pluralStringResource(
        state.periodKind.periodCountPlural(),
        state.periods.size,
        state.periods.size,
    )

    val listState: LazyListState = rememberLazyListState()
    GScreen(
        topBar = {
            GTopBar(
                title = stringResource(R.string.setup_periods_title, state.schoolYearLabel),
                subtitle = periodKindCountLabel,
                onBackClick = { onIntent(PeriodsUiIntent.BackClicked) },
                isContentScrolled = listState.canScrollBackward,
            )
        },
        modifier = modifier,
        bottomAction = {
            GButton(
                text = "Guardar",
                onClick = { onIntent(PeriodsUiIntent.SaveClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canSave,
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
            if (message != null) {
                item {
                    GBanner(
                        text = message,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = GemaSpacing.medium),
                        tone = GBannerTone.ERROR,
                        actionText = "Entendido",
                        onActionClick = onMessageDismissed,
                    )
                }
            }
            items(state.periods, key = { it.id.value }) { row ->
                PeriodEditor(
                    row = row,
                    isStartOverlapping = row.id in state.overlappingStartIds,
                    isEndOverlapping = row.id in state.overlappingEndIds,
                    onIntent = onIntent,
                )
                GDivider()
            }
            if (overlapErrorText != null) {
                item {
                    GBanner(
                        text = overlapErrorText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = GemaSpacing.medium)
                            .semantics { liveRegion = LiveRegionMode.Polite },
                        tone = GBannerTone.ERROR,
                        hasLeadingDot = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodEditor(
    row: PeriodRow,
    isStartOverlapping: Boolean,
    isEndOverlapping: Boolean,
    onIntent: (PeriodsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val startDateLabel: String = stringResource(R.string.setup_year_date_start_label)
    val endDateLabel: String = stringResource(R.string.setup_year_date_end_label)
    val startDateContentDescription: String = stringResource(
        R.string.setup_periods_date_field_content_description,
        row.label,
        startDateLabel,
    )
    val endDateContentDescription: String = stringResource(
        R.string.setup_periods_date_field_content_description,
        row.label,
        endDateLabel,
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = GemaSpacing.rowGap),
        verticalArrangement = Arrangement.spacedBy(GemaSpacing.small),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {},
            horizontalArrangement = Arrangement.spacedBy(GemaSpacing.rowGap),
        ) {
            GText(
                text = row.label,
                style = GTextStyle.TITLE_MEDIUM,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (row.isCurrent) {
                GBadge(text = stringResource(R.string.setup_periods_current_badge))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        ) {
            GDateField(
                value = row.startDate,
                onValueChange = { onIntent(PeriodsUiIntent.StartDateChanged(row.id, it)) },
                label = startDateLabel,
                isError = isStartOverlapping,
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) { contentDescription = startDateContentDescription },
            )
            GDateField(
                value = row.endDate,
                onValueChange = { onIntent(PeriodsUiIntent.EndDateChanged(row.id, it)) },
                label = endDateLabel,
                isError = isEndOverlapping,
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) { contentDescription = endDateContentDescription },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PeriodsScreenPreview() {
    GemaTheme {
        PeriodsScreen(
            state = PeriodsUiState(
                isLoading = false,
                schoolYearLabel = "2026",
                periodKind = PeriodKind.BIMESTER,
                periods = listOf(
                    PeriodRow(
                        PeriodId("1"),
                        1,
                        "I Bimestre",
                        LocalDate.of(2026, 3, 2),
                        LocalDate.of(2026, 5, 13),
                        true,
                    ),
                ),
                canSave = true,
            ),
            onIntent = {},
        )
    }
}
