@file:Suppress("MatchingDeclarationName")

package com.emm.gema.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.theme.GemaBorder
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

private const val UNWORKED_COMMENT_MARK: String = "*"
private const val INCOMPLETE_MARK: String = "!"
private const val EMPTY_MARK: String = ""

enum class GLevelChipSize { GRID, INLINE }

@Composable
fun GLevelChip(
    level: AchievementLevel?,
    modifier: Modifier = Modifier,
    hasUnworkedComment: Boolean = false,
    isIncomplete: Boolean = false,
    isCurrent: Boolean = false,
    size: GLevelChipSize = GLevelChipSize.INLINE,
    onClick: (() -> Unit)? = null,
) {
    val width: Dp = when (size) {
        GLevelChipSize.GRID -> GemaSpacing.gridCellWidth
        GLevelChipSize.INLINE -> GemaSpacing.minimumTouchTarget
    }
    val height: Dp = when (size) {
        GLevelChipSize.GRID -> GemaSpacing.gridChipHeight
        GLevelChipSize.INLINE -> GemaSpacing.minimumTouchTarget
    }
    val markerColor: Color = MaterialTheme.colorScheme.error
    val shape = GemaShapes.control
    val border = BorderStroke(
        width = if (isCurrent) GemaSpacing.indicatorStroke else GemaBorder.hairline,
        color = when {
            isIncomplete -> markerColor
            isCurrent -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        },
    )
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .width(width)
                .height(height)
                .semantics { contentDescription = describe(level, hasUnworkedComment, isIncomplete) },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label(level, hasUnworkedComment),
                style = when (size) {
                    GLevelChipSize.GRID -> MaterialTheme.typography.labelSmall
                    GLevelChipSize.INLINE -> MaterialTheme.typography.labelLarge
                },
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            if (isIncomplete) {
                Text(
                    text = INCOMPLETE_MARK,
                    style = MaterialTheme.typography.labelSmall,
                    color = markerColor,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    if (onClick == null) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = border,
            content = content,
        )
    } else {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = border,
            content = content,
        )
    }
}

private fun label(level: AchievementLevel?, hasUnworkedComment: Boolean): String = when {
    level != null -> level.name
    hasUnworkedComment -> UNWORKED_COMMENT_MARK
    else -> EMPTY_MARK
}

private fun describe(level: AchievementLevel?, hasUnworkedComment: Boolean, isIncomplete: Boolean): String = when {
    isIncomplete -> "En inicio sin conclusión descriptiva"
    level != null -> level.officialName
    hasUnworkedComment -> "Competencia no evaluada"
    else -> "Sin nivel"
}

@PreviewLightDark
@Composable
private fun GLevelChipPreview() {
    GemaTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small)) {
            GLevelChip(level = AchievementLevel.AD, size = GLevelChipSize.GRID)
            GLevelChip(level = AchievementLevel.C, isIncomplete = true, size = GLevelChipSize.GRID)
            GLevelChip(level = null, hasUnworkedComment = true, size = GLevelChipSize.GRID)
            GLevelChip(level = null, size = GLevelChipSize.GRID)
            GLevelChip(level = AchievementLevel.B, isCurrent = true, size = GLevelChipSize.GRID)
        }
    }
}
