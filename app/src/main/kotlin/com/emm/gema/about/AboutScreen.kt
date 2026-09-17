package com.emm.gema.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.R
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar

@Composable
fun AboutScreen(state: AboutUiState, onIntent: (AboutUiIntent) -> Unit, modifier: Modifier = Modifier) {
    GScreen(
        modifier = modifier,
        topBar = {
            GTopBar(
                title = stringResource(R.string.about_title),
                onBackClick = { onIntent(AboutUiIntent.BackClicked) },
            )
        },
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.medium),
        ) {
            DisclaimerBanner()
            SourcesSection(onIntent = onIntent)
            GText(
                text = stringResource(R.string.about_version, state.version),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = GemaSpacing.screenGutter, vertical = GemaSpacing.medium),
                style = GTextStyle.LABEL_SMALL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DisclaimerBanner() {
    GBanner(
        text = null,
        modifier = Modifier.padding(horizontal = GemaSpacing.screenGutter, vertical = GemaSpacing.medium),
        tone = GBannerTone.INFO,
        message = {
            GText(
                text = stringResource(R.string.about_disclaimer_heading),
                modifier = Modifier.semantics { heading() },
                style = GTextStyle.LABEL_SMALL,
            )
            GText(
                text = stringResource(R.string.about_disclaimer_body),
                style = GTextStyle.BODY_MEDIUM,
            )
        },
    )
}

@Composable
private fun SourcesSection(onIntent: (AboutUiIntent) -> Unit) {
    Column {
        AboutSource.entries.forEach { source ->
            GListItem(
                title = sourceTitle(source),
                subtitle = sourceDestination(source),
                modifier = Modifier.fillMaxWidth(),
                hasChevron = true,
                onClick = { onIntent(AboutUiIntent.SourceLinkClicked(source)) },
            )
        }
    }
}

@Composable
private fun sourceTitle(source: AboutSource): String = when (source) {
    AboutSource.CNEB -> stringResource(R.string.about_source_cneb_title)
    AboutSource.PRIMARY_CURRICULUM -> stringResource(R.string.about_source_primary_curriculum_title)
    AboutSource.SIAGIE -> stringResource(R.string.about_source_siagie_title)
    AboutSource.MINEDU -> stringResource(R.string.about_source_minedu_title)
}

@Composable
private fun sourceDestination(source: AboutSource): String = when (source) {
    AboutSource.CNEB -> stringResource(R.string.about_source_cneb_destination)
    AboutSource.PRIMARY_CURRICULUM -> stringResource(R.string.about_source_primary_curriculum_destination)
    AboutSource.SIAGIE -> stringResource(R.string.about_source_siagie_destination)
    AboutSource.MINEDU -> stringResource(R.string.about_source_minedu_destination)
}

@PreviewLightDark
@Composable
private fun AboutScreenPreview() {
    GemaTheme {
        AboutScreen(state = AboutUiState(version = "1.0"), onIntent = {})
    }
}
