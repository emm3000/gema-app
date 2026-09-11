package com.emm.gema.feature.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.backup.MAXIMUM_REMINDER_THRESHOLD_DAYS
import com.emm.gema.core.domain.backup.MINIMUM_REMINDER_THRESHOLD_DAYS
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GCard
import com.emm.gema.core.ui.GDialog
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GTextField
import com.emm.gema.core.ui.GTopBar
import java.time.format.DateTimeFormatter

private val lastBackupDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun BackupScreen(
    state: BackupUiState,
    onIntent: (BackupUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
) {
    GScreen(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        topBar = {
            GTopBar(
                title = stringResource(R.string.backup_title),
                onBackClick = { onIntent(BackupUiIntent.BackClicked) },
            )
        },
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.medium),
        ) {
            LastBackupCard(state = state, onIntent = onIntent)
            RestoreSection(state = state, onIntent = onIntent)
            ReminderSection(state = state, onIntent = onIntent)
        }
    }

    val confirmation: RestoreConfirmation? = state.restoreConfirmation
    if (confirmation != null) {
        RestoreDialog(confirmation = confirmation, onIntent = onIntent)
    }
}

@Composable
private fun LastBackupCard(state: BackupUiState, onIntent: (BackupUiIntent) -> Unit) {
    GCard {
        Text(
            text = stringResource(R.string.backup_last_title),
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = lastBackupLabel(state),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        GButton(
            text = stringResource(R.string.backup_create),
            onClick = { onIntent(BackupUiIntent.CreateBackupClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = GemaSpacing.medium),
            isBusy = state.isCreating,
        )
        Text(
            text = stringResource(R.string.backup_create_hint),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = GemaSpacing.small),
        )
    }
}

@Composable
private fun RestoreSection(state: BackupUiState, onIntent: (BackupUiIntent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(GemaSpacing.small)) {
        Text(
            text = stringResource(R.string.backup_restore_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        GButton(
            text = stringResource(R.string.backup_restore_choose),
            onClick = { onIntent(BackupUiIntent.ChooseRestoreFileClicked) },
            modifier = Modifier.fillMaxWidth(),
            variant = GButtonVariant.SECONDARY,
            isBusy = state.isRestoring,
        )
        GBanner(
            text = stringResource(R.string.backup_restore_warning),
            tone = GBannerTone.WARNING,
        )
    }
}

@Composable
private fun ReminderSection(state: BackupUiState, onIntent: (BackupUiIntent) -> Unit) {
    Row(
        modifier = Modifier.padding(bottom = GemaSpacing.large),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
    ) {
        GTextField(
            value = state.reminderThresholdInput,
            onValueChange = { value -> onIntent(BackupUiIntent.ReminderThresholdChanged(value)) },
            label = stringResource(R.string.backup_reminder_label),
            modifier = Modifier.width(GemaSpacing.narrowFieldWidth),
            keyboardType = KeyboardType.Number,
            errorText = reminderThresholdError(state),
        )
        Text(
            text = stringResource(R.string.backup_reminder_days),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RestoreDialog(confirmation: RestoreConfirmation, onIntent: (BackupUiIntent) -> Unit) {
    GDialog(
        title = stringResource(R.string.backup_restore_confirm_title),
        confirmText = stringResource(R.string.backup_restore_confirm),
        onConfirm = { onIntent(BackupUiIntent.RestoreConfirmed) },
        onDismiss = { onIntent(BackupUiIntent.RestoreDismissed) },
        dismissText = stringResource(R.string.backup_restore_cancel),
        isDestructive = true,
    ) {
        Text(
            text = stringResource(R.string.backup_restore_confirm_file, confirmation.fileName),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = pluralStringResource(
                R.plurals.backup_restore_confirm_school_years,
                confirmation.currentSchoolYearCount,
                confirmation.currentSchoolYearCount,
            ),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = GemaSpacing.small),
        )
        Text(
            text = stringResource(R.string.backup_restore_warning),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = GemaSpacing.small),
        )
    }
}

@Composable
private fun lastBackupLabel(state: BackupUiState): String {
    val days: Int = state.daysSinceLastBackup ?: return stringResource(R.string.backup_last_never)
    val date: String = state.lastBackupDate?.format(lastBackupDateFormat).orEmpty()
    val elapsed: String = if (days == 0) {
        stringResource(R.string.backup_last_today)
    } else {
        pluralStringResource(R.plurals.backup_last_days_ago, days, days)
    }
    return "$elapsed - $date"
}

@Composable
private fun reminderThresholdError(state: BackupUiState): String? = when {
    state.isReminderThresholdInvalid -> stringResource(
        R.string.backup_reminder_error,
        MINIMUM_REMINDER_THRESHOLD_DAYS,
        MAXIMUM_REMINDER_THRESHOLD_DAYS,
    )

    else -> null
}

@PreviewLightDark
@Composable
private fun BackupScreenPreview() {
    GemaTheme {
        BackupScreen(
            state = BackupUiState(isLoading = false, daysSinceLastBackup = 12),
            onIntent = {},
        )
    }
}
