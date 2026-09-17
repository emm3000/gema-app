package com.emm.gema.about

import android.content.ActivityNotFoundException
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun AboutRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AboutViewModel = koinViewModel(),
) {
    val state: State<AboutUiState> = viewModel.state.collectAsStateWithLifecycle()
    val uriHandler: UriHandler = LocalUriHandler.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is AboutUiEffect.OpenUrl -> uriHandler.openUrlSafely(effect.url)
                AboutUiEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    AboutScreen(state = state.value, onIntent = viewModel::onIntent, modifier = modifier)
}

private fun UriHandler.openUrlSafely(url: String) {
    try {
        openUri(url)
    } catch (expected: ActivityNotFoundException) {
        return
    }
}
