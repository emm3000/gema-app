package com.emm.gema.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaBorder
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GYearCard(
    label: String,
    subtitle: String,
    sectionCountLabel: String,
    periodsLabel: String,
    isActive: Boolean,
    badgeText: String?,
    onClick: () -> Unit,
    onPeriodsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = GemaShapes.container,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isActive) GemaSpacing.activeBorder else GemaBorder.hairline,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Column(
            modifier = Modifier.padding(GemaSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.extraSmall),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (badgeText != null) {
                    GBadge(text = badgeText)
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider(
                modifier = Modifier.padding(top = GemaSpacing.extraSmall),
                color = MaterialTheme.colorScheme.outline,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = sectionCountLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.clickable(onClick = onPeriodsClick),
                    horizontalArrangement = Arrangement.spacedBy(GemaSpacing.extraSmall),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = periodsLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun GYearCardPreview() {
    GemaTheme {
        Column(verticalArrangement = Arrangement.spacedBy(GemaSpacing.small)) {
            GYearCard(
                label = "2026",
                subtitle = "01/03 – 20/12 · 4 bimestres",
                sectionCountLabel = "2 secciones",
                periodsLabel = "Periodos",
                isActive = true,
                badgeText = "ACTIVO",
                onClick = {},
                onPeriodsClick = {},
            )
            GYearCard(
                label = "2025",
                subtitle = "01/03 – 19/12 · 3 trimestres",
                sectionCountLabel = "1 sección",
                periodsLabel = "Periodos",
                isActive = false,
                badgeText = null,
                onClick = {},
                onPeriodsClick = {},
            )
        }
    }
}
