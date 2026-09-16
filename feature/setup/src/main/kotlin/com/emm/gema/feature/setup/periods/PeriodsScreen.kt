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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBadge
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GBorderedContainer
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GDateField
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
                showHairline = listState.canScrollBackward,
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
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.medium),
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
            items(state.periods, key = { it.id.value }) { row ->
                PeriodEditor(
                    row = row,
                    isStartOverlapping = row.id in state.overlappingStartIds,
                    isEndOverlapping = row.id in state.overlappingEndIds,
                    onIntent = onIntent,
                )
            }
            if (overlapErrorText != null) {
                item {
                    GBanner(
                        text = overlapErrorText,
                        modifier = Modifier.fillMaxWidth(),
                        tone = GBannerTone.WARNING,
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
    GBorderedContainer(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(GemaSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                horizontalArrangement = Arrangement.spacedBy(GemaSpacing.medium),
            ) {
                GDateField(
                    value = row.startDate,
                    onValueChange = { onIntent(PeriodsUiIntent.StartDateChanged(row.id, it)) },
                    isError = isStartOverlapping,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
                GDateField(
                    value = row.endDate,
                    onValueChange = { onIntent(PeriodsUiIntent.EndDateChanged(row.id, it)) },
                    isError = isEndOverlapping,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
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
