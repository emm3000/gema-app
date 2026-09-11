package com.emm.gema.feature.sections.areas

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.theme.GemaBorder
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GDivider
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GSwitchRow
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.feature.sections.R

@Composable
fun SectionAreasScreen(
    state: SectionAreasUiState,
    onIntent: (SectionAreasUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    onMessageDismissed: () -> Unit = {},
) {
    GScreen(
        topBar = {
            GTopBar(
                title = stringResource(R.string.sections_areas_title, state.sectionTitle),
                subtitle = stringResource(R.string.sections_areas_subtitle),
                onBackClick = { onIntent(SectionAreasUiIntent.BackClicked) },
            )
        },
        modifier = modifier,
    ) { padding: PaddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(top = GemaSpacing.small, bottom = GemaSpacing.screenGutter),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.medium),
        ) {
            if (message != null) {
                item {
                    GBanner(
                        text = message,
                        modifier = Modifier.fillMaxWidth(),
                        tone = GBannerTone.ERROR,
                        actionText = stringResource(R.string.sections_areas_understood),
                        onActionClick = onMessageDismissed,
                    )
                }
            }
            item {
                GText(
                    text = stringResource(R.string.sections_areas_hint),
                    style = GTextStyle.BODY_MEDIUM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                AreaSwitchList(areas = state.areas, onIntent = onIntent)
            }
            items(
                state.areas.filter { it.recordedLevelCount > 0 },
                key = { "recorded-${it.id.name}" },
            ) { row ->
                GBanner(
                    text = pluralStringResource(
                        R.plurals.sections_areas_recorded_levels,
                        row.recordedLevelCount,
                        row.name,
                        row.recordedLevelCount,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    tone = GBannerTone.WARNING,
                    icon = Icons.Filled.Warning,
                )
            }
        }
    }
}

@Composable
private fun AreaSwitchList(
    areas: List<AreaToggleRow>,
    onIntent: (SectionAreasUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(GemaShapes.container)
            .border(GemaBorder.hairline, MaterialTheme.colorScheme.outlineVariant, GemaShapes.container),
    ) {
        areas.forEachIndexed { index, row ->
            GSwitchRow(
                title = row.name,
                isChecked = row.isActive,
                onCheckedChange = { onIntent(SectionAreasUiIntent.AreaToggled(row.id, it)) },
                modifier = Modifier.fillMaxWidth(),
            )
            if (index < areas.lastIndex) {
                GDivider()
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SectionAreasScreenPreview() {
    GemaTheme {
        SectionAreasScreen(
            state = SectionAreasUiState(
                isLoading = false,
                sectionTitle = "3° A",
                areas = listOf(
                    AreaToggleRow(Area.COMU, "Comunicación", true, 0),
                    AreaToggleRow(Area.EFIS, "Educación Física", false, 12),
                ),
            ),
            onIntent = {},
        )
    }
}
