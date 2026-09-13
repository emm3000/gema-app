package com.emm.gema.core.ui

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String?,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    errorText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isEnabled: Boolean = true,
    isSingleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.heightIn(min = GemaSpacing.minimumTouchTarget),
        label = label?.let { { Text(text = it, style = MaterialTheme.typography.labelSmall) } },
        enabled = isEnabled,
        singleLine = isSingleLine,
        isError = errorText != null,
        shape = GemaShapes.control,
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        supportingText = supportingLine(errorText ?: supportingText, errorText != null),
    )
}

private fun supportingLine(text: String?, isError: Boolean): (@Composable () -> Unit)? {
    if (text == null) return null
    return {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@PreviewLightDark
@Composable
private fun GTextFieldPreview() {
    GemaTheme {
        GTextField(
            value = "7",
            onValueChange = {},
            label = "Recordarme cada",
            supportingText = "días",
            keyboardType = KeyboardType.Number,
        )
    }
}

@PreviewLightDark
@Composable
private fun GTextFieldMultiLinePreview() {
    GemaTheme {
        GTextField(
            value = "En inicio, reconoce algunos números hasta el 20.",
            onValueChange = {},
            label = "Conclusión descriptiva",
            isSingleLine = false,
        )
    }
}
