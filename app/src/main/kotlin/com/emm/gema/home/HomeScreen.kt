package com.emm.gema.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.emm.gema.R
import com.emm.gema.core.ui.GEmptyState

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    GEmptyState(
        title = stringResource(R.string.home_empty_title),
        message = stringResource(R.string.home_empty_message),
        modifier = modifier,
    )
}
