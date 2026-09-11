package com.emm.gema.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.emm.gema.R
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GEmptyState

@Composable
fun HomeScreen(
    state: HomeUiState,
    onIntent: (HomeUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(GemaSpacing.screenGutter),
        verticalArrangement = Arrangement.spacedBy(GemaSpacing.medium),
    ) {
        val reminder: BackupReminder? = state.backupReminder
        if (reminder != null) {
            GBanner(
                text = reminderText(reminder),
                tone = GBannerTone.WARNING,
                actionText = stringResource(R.string.home_backup_reminder_action),
                onActionClick = { onIntent(HomeUiIntent.BackupReminderClicked) },
            )
        }
        GEmptyState(
            title = stringResource(R.string.home_empty_title),
            message = stringResource(R.string.home_empty_message),
        )
    }
}

@Composable
private fun reminderText(reminder: BackupReminder): String = if (reminder.hasEverBackedUp) {
    pluralStringResource(
        R.plurals.home_backup_reminder_days,
        reminder.daysSinceLastBackup,
        reminder.daysSinceLastBackup,
    )
} else {
    stringResource(R.string.home_backup_reminder_never)
}
