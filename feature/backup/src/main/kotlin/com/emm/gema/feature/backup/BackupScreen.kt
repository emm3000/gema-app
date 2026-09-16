package com.emm.gema.feature.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
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
import com.emm.gema.core.theme.asDayMonthYear
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GCard
import com.emm.gema.core.ui.GDialog
import com.emm.gema.core.ui.GDivider
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextField
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar

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
                subtitle = stringResource(R.string.backup_subtitle),
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
            GDivider()
            RestoreSection(state = state, onIntent = onIntent)
            GDivider()
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
    Column(verticalArrangement = Arrangement.spacedBy(GemaSpacing.small)) {
        if (state.isBackupOverdue) {
            GBanner(
                title = overdueBackupTitle(state),
                text = overdueBackupSubtitle(state),
                tone = GBannerTone.ERROR,
                icon = Icons.Filled.Warning,
            )
        } else {
            GCard {
                GText(
                    text = stringResource(R.string.backup_last_title),
                    style = GTextStyle.LABEL_SMALL,
                )
                GText(
                    text = lastBackupLabel(state),
                    style = GTextStyle.TITLE_MEDIUM,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        GButton(
            text = stringResource(R.string.backup_create),
            onClick = { onIntent(BackupUiIntent.CreateBackupClicked) },
            modifier = Modifier.fillMaxWidth(),
            isBusy = state.isCreating,
            icon = Icons.Filled.Upload,
        )
        GText(
            text = stringResource(R.string.backup_create_hint),
            style = GTextStyle.LABEL_SMALL,
        )
    }
}

@Composable
private fun RestoreSection(state: BackupUiState, onIntent: (BackupUiIntent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(GemaSpacing.small)) {
        GText(
            text = stringResource(R.string.backup_restore_title),
            style = GTextStyle.LABEL_SMALL,
        )
        GButton(
            text = stringResource(R.string.backup_restore_choose),
            onClick = { onIntent(BackupUiIntent.ChooseRestoreFileClicked) },
            modifier = Modifier.fillMaxWidth(),
            variant = GButtonVariant.SECONDARY,
            isBusy = state.isRestoring,
            icon = Icons.Filled.FolderOpen,
        )
        GBanner(
            text = stringResource(R.string.backup_restore_warning),
            tone = GBannerTone.WARNING,
        )
    }
}

@Composable
private fun ReminderSection(state: BackupUiState, onIntent: (BackupUiIntent) -> Unit) {
    Column(
        modifier = Modifier.padding(bottom = GemaSpacing.large),
        verticalArrangement = Arrangement.spacedBy(GemaSpacing.small),
    ) {
        GText(
            text = stringResource(R.string.backup_reminder_label),
            style = GTextStyle.LABEL_SMALL,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        ) {
            GTextField(
                value = state.reminderThresholdInput,
                onValueChange = { value -> onIntent(BackupUiIntent.ReminderThresholdChanged(value)) },
                label = null,
                modifier = Modifier.width(GemaSpacing.narrowFieldWidth),
                keyboardType = KeyboardType.Number,
                errorText = reminderThresholdError(state),
            )
            GText(
                text = stringResource(R.string.backup_reminder_days),
                style = GTextStyle.BODY_LARGE,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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
        GText(
            text = stringResource(R.string.backup_restore_confirm_file, confirmation.fileName),
            style = GTextStyle.BODY_LARGE,
        )
        GText(
            text = pluralStringResource(
                R.plurals.backup_restore_confirm_school_years,
                confirmation.currentSchoolYearCount,
                confirmation.currentSchoolYearCount,
            ),
            style = GTextStyle.BODY_LARGE,
            modifier = Modifier.padding(top = GemaSpacing.small),
        )
        GText(
            text = stringResource(R.string.backup_restore_warning),
            style = GTextStyle.BODY_LARGE,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = GemaSpacing.small),
        )
    }
}

@Composable
private fun lastBackupLabel(state: BackupUiState): String {
    val days: Int = state.daysSinceLastBackup ?: return stringResource(R.string.backup_last_never)
    val date: String = state.lastBackupDate?.asDayMonthYear().orEmpty()
    val elapsed: String = if (days == 0) {
        stringResource(R.string.backup_last_today)
    } else {
        pluralStringResource(R.plurals.backup_last_days_ago, days, days)
    }
    return "$elapsed · $date"
}

@Composable
private fun overdueBackupTitle(state: BackupUiState): String {
    val days: Int = state.daysSinceLastBackup ?: 0
    val elapsed: String = pluralStringResource(R.plurals.backup_last_days_ago, days, days)
    return "${stringResource(R.string.backup_last_title)} $elapsed"
}

@Composable
private fun overdueBackupSubtitle(state: BackupUiState): String {
    val date: String = state.lastBackupDate?.asDayMonthYear().orEmpty()
    return stringResource(R.string.backup_overdue_subtitle, date, state.reminderThresholdDays)
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
            state = BackupUiState(isLoading = false, daysSinceLastBackup = 2),
            onIntent = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun BackupScreenOverduePreview() {
    GemaTheme {
        BackupScreen(
            state = BackupUiState(isLoading = false, daysSinceLastBackup = 12, isBackupOverdue = true),
            onIntent = {},
        )
    }
}
