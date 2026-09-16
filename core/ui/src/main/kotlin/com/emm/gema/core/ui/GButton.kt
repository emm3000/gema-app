package com.emm.gema.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

private const val BUSY_ALPHA: Float = 0.85f

@Composable
fun GButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: GButtonVariant = GButtonVariant.PRIMARY,
    enabled: Boolean = true,
    isBusy: Boolean = false,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    val buttonModifier: Modifier = modifier
        .heightIn(min = GemaSpacing.minimumTouchTarget)
        .alpha(if (isBusy) BUSY_ALPHA else 1f)
    val isClickable: Boolean = enabled && !isBusy
    val disabledContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh
    val disabledContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant

    when (variant) {
        GButtonVariant.PRIMARY -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = isClickable,
            shape = GemaShapes.control,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = disabledContainerColor,
                disabledContentColor = disabledContentColor,
            ),
        ) {
            GButtonLabel(text = text, isBusy = isBusy, icon = icon, trailingIcon = trailingIcon)
        }

        GButtonVariant.SECONDARY -> OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = isClickable,
            shape = GemaShapes.control,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = disabledContainerColor,
                disabledContentColor = disabledContentColor,
            ),
        ) {
            GButtonLabel(text = text, isBusy = isBusy, icon = icon, trailingIcon = trailingIcon)
        }

        GButtonVariant.DESTRUCTIVE -> OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = isClickable,
            shape = GemaShapes.control,
            border = BorderStroke(
                width = 1.dp,
                color = if (isClickable) MaterialTheme.colorScheme.error else disabledContentColor,
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
                disabledContainerColor = disabledContainerColor,
                disabledContentColor = disabledContentColor,
            ),
        ) {
            GButtonLabel(text = text, isBusy = isBusy, icon = icon, trailingIcon = trailingIcon)
        }

        GButtonVariant.TEXT -> TextButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = isClickable,
            shape = GemaShapes.control,
            colors = ButtonDefaults.textButtonColors(
                disabledContentColor = disabledContentColor,
            ),
        ) {
            GButtonLabel(text = text, isBusy = isBusy, icon = icon, trailingIcon = trailingIcon)
        }
    }
}

@Composable
private fun GButtonLabel(text: String, isBusy: Boolean, icon: ImageVector?, trailingIcon: ImageVector?) {
    if (isBusy) {
        CircularProgressIndicator(
            modifier = Modifier.size(GemaSpacing.medium),
            strokeWidth = GemaSpacing.indicatorStroke,
            color = LocalContentColor.current,
        )
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(GemaSpacing.buttonIconSize),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
            )
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(GemaSpacing.buttonIconSize),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun GButtonPreview() {
    GemaTheme {
        GButton(text = "Crear año escolar", onClick = {})
    }
}

@PreviewLightDark
@Composable
private fun GButtonWithIconPreview() {
    GemaTheme {
        GButton(text = "Exportar el mes", onClick = {}, icon = Icons.Filled.Upload)
    }
}
