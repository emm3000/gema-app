package com.emm.gema.feature.sections.areas

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.theme.GemaAccents
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
    val listState: LazyListState = rememberLazyListState()
    GScreen(
        topBar = {
            GTopBar(
                title = stringResource(R.string.sections_areas_title, state.sectionTitle),
                subtitle = stringResource(R.string.sections_areas_subtitle),
                onBackClick = { onIntent(SectionAreasUiIntent.BackClicked) },
                isContentScrolled = listState.canScrollBackward,
            )
        },
        modifier = modifier,
    ) { padding: PaddingValues ->
        LazyColumn(
            state = listState,
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
                    style = GTextStyle.BODY_LARGE,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                AreaSwitchList(areas = state.areas, onIntent = onIntent)
            }
        }
    }
}

internal fun recordedLevelsNoteCount(isActive: Boolean, recordedLevelCount: Int): Int? =
    recordedLevelCount.takeIf { !isActive && it > 0 }

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
            val recordedLevelsCount: Int? = recordedLevelsNoteCount(row.isActive, row.recordedLevelCount)
            val recordedLevelsNote: String? = recordedLevelsCount?.let {
                pluralStringResource(R.plurals.sections_areas_recorded_levels, it, it)
            }
            GSwitchRow(
                title = row.name,
                isChecked = row.isActive,
                onCheckedChange = { onIntent(SectionAreasUiIntent.AreaToggled(row.id, it)) },
                modifier = Modifier.fillMaxWidth(),
                subtitle = recordedLevelsNote,
                subtitleColor = if (recordedLevelsNote != null) {
                    GemaAccents.onWarningContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
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
                sectionTitle = "3ro A",
                areas = listOf(
                    AreaToggleRow(Area.COMU, "Comunicación", true, 0),
                    AreaToggleRow(Area.EFIS, "Educación Física", false, 12),
                    AreaToggleRow(Area.INGLES_EXT, "Inglés", false, 5),
                ),
            ),
            onIntent = {},
        )
    }
}
