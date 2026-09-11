package com.emm.gema.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

@Composable
fun GScreen(
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    bottomAction: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = topBar,
        bottomBar = {
            bottomAction?.let { action ->
                Box(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                        .padding(horizontal = GemaSpacing.screenGutter),
                ) {
                    action()
                }
            }
        },
        snackbarHost = { snackbarHostState?.let { state -> SnackbarHost(hostState = state) } },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { scaffoldPadding ->
        Box(modifier = Modifier.padding(horizontal = GemaSpacing.screenGutter)) {
            content(scaffoldPadding)
        }
    }
}

@PreviewLightDark
@Composable
private fun GScreenPreview() {
    GemaTheme {
        GScreen(
            topBar = { GTopBar(title = "Año escolar") },
            bottomAction = {
                GButton(text = "Continuar", onClick = {}, modifier = Modifier.fillMaxWidth())
            },
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                GText(text = "Contenido", style = GTextStyle.BODY_MEDIUM)
            }
        }
    }
}
