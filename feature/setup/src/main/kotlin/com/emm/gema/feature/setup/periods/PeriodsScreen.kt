package com.emm.gema.feature.setup.periods

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBadge
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GCard
import com.emm.gema.core.ui.GDateField
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
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
    val periodKindCountLabel: String = periodKindCountLabel(state.periodKindLabel, state.periods.size)

    GScreen(
        topBar = {
            GTopBar(
                title = "Periodos ${state.schoolYearLabel}",
                subtitle = periodKindCountLabel,
                onBackClick = { onIntent(PeriodsUiIntent.BackClicked) },
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
                    isOverlapping = row.id in state.overlappingPeriodIds,
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

private fun periodKindCountLabel(periodKindLabel: String, count: Int): String {
    val lowercaseKindLabel: String = periodKindLabel.lowercase()
    val pluralSuffix: String = if (count == 1) "" else "s"
    return "$count $lowercaseKindLabel$pluralSuffix"
}

@Composable
private fun PeriodEditor(
    row: PeriodRow,
    isOverlapping: Boolean,
    onIntent: (PeriodsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    GCard(modifier = modifier.fillMaxWidth()) {
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
                    GBadge(text = "ACTUAL")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GemaSpacing.medium),
            ) {
                GDateField(
                    value = row.startDate,
                    onValueChange = { onIntent(PeriodsUiIntent.StartDateChanged(row.id, it)) },
                    isError = isOverlapping,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
                GDateField(
                    value = row.endDate,
                    onValueChange = { onIntent(PeriodsUiIntent.EndDateChanged(row.id, it)) },
                    isError = isOverlapping,
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
                periodKindLabel = "Bimestre",
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
