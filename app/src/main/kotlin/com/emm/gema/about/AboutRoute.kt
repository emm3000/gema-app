package com.emm.gema.about

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context: Context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is AboutUiEffect.OpenUrl -> openUrlOrIgnore {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(effect.url)))
                }
                AboutUiEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    AboutScreen(state = state.value, onIntent = viewModel::onIntent, modifier = modifier)
}

internal fun openUrlOrIgnore(open: () -> Unit) {
    try {
        open()
    } catch (expected: ActivityNotFoundException) {
        return
    }
}
