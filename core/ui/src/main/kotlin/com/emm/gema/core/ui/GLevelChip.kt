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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import com.emm.gema.core.theme.GemaBorder
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

private const val UNWORKED_COMMENT_MARK: String = "*"
private const val INCOMPLETE_MARK: String = "!"
private const val EMPTY_MARK: String = ""
private const val INCOMPLETE_DESCRIPTION: String = "En inicio sin conclusión descriptiva"
private const val UNWORKED_COMMENT_DESCRIPTION: String = "Competencia no evaluada"
private const val EMPTY_DESCRIPTION: String = "Sin nivel"

enum class GLevelChipSize { GRID, INLINE, EVIDENCE }

private data class GLevelChipMetrics(
    val width: Dp,
    val height: Dp,
    val shape: Shape,
    val labelStyle: TextStyle,
)

@Composable
private fun GLevelChipSize.metrics(): GLevelChipMetrics = when (this) {
    GLevelChipSize.GRID -> GLevelChipMetrics(
        width = GemaSpacing.gridCellWidth,
        height = GemaSpacing.gridChipHeight,
        shape = GemaShapes.control,
        labelStyle = MaterialTheme.typography.bodySmall,
    )
    GLevelChipSize.INLINE -> GLevelChipMetrics(
        width = GemaSpacing.minimumTouchTarget,
        height = GemaSpacing.minimumTouchTarget,
        shape = GemaShapes.control,
        labelStyle = MaterialTheme.typography.labelLarge,
    )
    GLevelChipSize.EVIDENCE -> GLevelChipMetrics(
        width = GemaSpacing.evidenceChipWidth,
        height = GemaSpacing.evidenceChipHeight,
        shape = GemaShapes.chip,
        labelStyle = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun borderColor(isIncomplete: Boolean, isCurrent: Boolean): Color = when {
    isIncomplete -> MaterialTheme.colorScheme.error
    isCurrent -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.outline
}

@Composable
fun GLevelChip(
    letter: String?,
    modifier: Modifier = Modifier,
    hasUnworkedComment: Boolean = false,
    isIncomplete: Boolean = false,
    isCurrent: Boolean = false,
    size: GLevelChipSize = GLevelChipSize.INLINE,
    onClick: (() -> Unit)? = null,
) {
    val metrics: GLevelChipMetrics = size.metrics()
    val backgroundColor: Color = MaterialTheme.colorScheme.surface
    val markerColor: Color = MaterialTheme.colorScheme.error
    val border = BorderStroke(
        width = if (isCurrent) GemaSpacing.indicatorStroke else GemaBorder.hairline,
        color = borderColor(isIncomplete, isCurrent),
    )
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .width(metrics.width)
                .height(metrics.height)
                .semantics { contentDescription = describe(letter, hasUnworkedComment, isIncomplete) },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label(letter, hasUnworkedComment),
                style = metrics.labelStyle,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            if (isIncomplete) {
                Text(
                    text = INCOMPLETE_MARK,
                    style = MaterialTheme.typography.bodySmall,
                    color = markerColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }

    if (onClick == null) {
        Surface(
            modifier = modifier,
            shape = metrics.shape,
            color = backgroundColor,
            border = border,
            content = content,
        )
    } else {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = metrics.shape,
            color = backgroundColor,
            border = border,
            content = content,
        )
    }
}

private fun label(letter: String?, hasUnworkedComment: Boolean): String = when {
    letter != null -> letter
    hasUnworkedComment -> UNWORKED_COMMENT_MARK
    else -> EMPTY_MARK
}

private fun describe(letter: String?, hasUnworkedComment: Boolean, isIncomplete: Boolean): String = when {
    isIncomplete -> INCOMPLETE_DESCRIPTION
    letter != null -> GLevelOption.entries.first { it.letter == letter }.contentDescription
    hasUnworkedComment -> UNWORKED_COMMENT_DESCRIPTION
    else -> EMPTY_DESCRIPTION
}

@PreviewLightDark
@Composable
private fun GLevelChipPreview() {
    GemaTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small)) {
            GLevelChip(letter = GLevelOption.AD.letter, size = GLevelChipSize.GRID)
            GLevelChip(letter = GLevelOption.C.letter, isIncomplete = true, size = GLevelChipSize.GRID)
            GLevelChip(letter = null, hasUnworkedComment = true, size = GLevelChipSize.GRID)
            GLevelChip(letter = null, size = GLevelChipSize.GRID)
            GLevelChip(letter = GLevelOption.B.letter, isCurrent = true, size = GLevelChipSize.GRID)
        }
    }
}
