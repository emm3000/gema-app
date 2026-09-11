package com.emm.gema.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.emm.gema.core.theme.GemaSpacing

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
        bottomBar = { bottomAction?.invoke() },
        snackbarHost = { snackbarHostState?.let { state -> SnackbarHost(hostState = state) } },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { scaffoldPadding ->
        Box(modifier = Modifier.padding(horizontal = GemaSpacing.screenGutter)) {
            content(scaffoldPadding)
        }
    }
}
