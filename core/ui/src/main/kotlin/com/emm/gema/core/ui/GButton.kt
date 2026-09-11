package com.emm.gema.core.ui

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: GButtonVariant = GButtonVariant.PRIMARY,
    enabled: Boolean = true,
    isBusy: Boolean = false,
    icon: ImageVector? = null,
) {
    val buttonModifier: Modifier = modifier.heightIn(min = GemaSpacing.minimumTouchTarget)
    val isClickable: Boolean = enabled && !isBusy

    when (variant) {
        GButtonVariant.PRIMARY -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = isClickable,
            shape = GemaShapes.control,
        ) {
            GButtonLabel(text = text, isBusy = isBusy, icon = icon)
        }

        GButtonVariant.SECONDARY -> OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = isClickable,
            shape = GemaShapes.control,
        ) {
            GButtonLabel(text = text, isBusy = isBusy, icon = icon)
        }

        GButtonVariant.DESTRUCTIVE -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = isClickable,
            shape = GemaShapes.control,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
        ) {
            GButtonLabel(text = text, isBusy = isBusy, icon = icon)
        }

        GButtonVariant.TEXT -> TextButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = isClickable,
            shape = GemaShapes.control,
        ) {
            GButtonLabel(text = text, isBusy = isBusy, icon = icon)
        }
    }
}

@Composable
private fun GButtonLabel(text: String, isBusy: Boolean, icon: ImageVector?) {
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
                Icon(imageVector = icon, contentDescription = null)
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
            )
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
