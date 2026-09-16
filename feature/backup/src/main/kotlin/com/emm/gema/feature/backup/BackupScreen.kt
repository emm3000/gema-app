package com.emm.gema.feature.backup

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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
    val scrollState: ScrollState = rememberScrollState()
    GScreen(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        topBar = {
            GTopBar(
                title = stringResource(R.string.backup_title),
                subtitle = stringResource(R.string.backup_subtitle),
                onBackClick = { onIntent(BackupUiIntent.BackClicked) },
                isContentScrolled = scrollState.value > 0,
            )
        },
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .padding(scaffoldPadding)
                .verticalScroll(scrollState),
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
        val overdueDescription: String = stringResource(R.string.backup_last_overdue_description)
        GBanner(
            text = null,
            modifier = Modifier.semantics(mergeDescendants = true) {
                if (state.isBackupOverdue) stateDescription = overdueDescription
            },
            tone = if (state.isBackupOverdue) GBannerTone.ERROR else GBannerTone.INFO,
            hasLeadingDot = state.isBackupOverdue,
            message = { LastBackupMessage(state = state) },
        )
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
            modifier = Modifier.semantics { heading() },
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
            text = stringResource(R.string.backup_reminder_title),
            style = GTextStyle.LABEL_SMALL,
            modifier = Modifier.semantics { heading() },
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        ) {
            GTextField(
                value = state.reminderThresholdInput,
                onValueChange = { value -> onIntent(BackupUiIntent.ReminderThresholdChanged(value)) },
                label = stringResource(R.string.backup_reminder_label),
                modifier = Modifier.width(GemaSpacing.compactFieldWidth),
                keyboardType = KeyboardType.Number,
                errorText = reminderThresholdError(state),
            )
            GText(
                text = stringResource(R.string.backup_reminder_days),
                style = GTextStyle.BODY_LARGE,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        GText(
            text = stringResource(R.string.backup_reminder_helper),
            style = GTextStyle.BODY_SMALL,
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
        GText(
            text = stringResource(R.string.backup_restore_confirm_file, confirmation.fileName),
            style = GTextStyle.BODY_LARGE,
        )
        Column(modifier = Modifier.fillMaxWidth().padding(top = GemaSpacing.small)) {
            GDivider()
            RestoreCountRow(
                count = confirmation.currentSchoolYearCount,
                label = pluralStringResource(
                    R.plurals.backup_restore_confirm_row_school_years,
                    confirmation.currentSchoolYearCount,
                ),
                rowDescription = pluralStringResource(
                    R.plurals.backup_restore_confirm_school_years_description,
                    confirmation.currentSchoolYearCount,
                    confirmation.currentSchoolYearCount,
                ),
            )
            GDivider()
            RestoreCountRow(
                count = confirmation.currentStudentCount,
                label = pluralStringResource(
                    R.plurals.backup_restore_confirm_row_students,
                    confirmation.currentStudentCount,
                ),
                rowDescription = pluralStringResource(
                    R.plurals.backup_restore_confirm_students_description,
                    confirmation.currentStudentCount,
                    confirmation.currentStudentCount,
                ),
            )
            GDivider()
        }
        GText(
            text = stringResource(R.string.backup_restore_confirm_restart),
            style = GTextStyle.BODY_SMALL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = GemaSpacing.small),
        )
    }
}

@Composable
private fun RestoreCountRow(count: Int, label: String, rowDescription: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = GemaSpacing.compactLineHeight)
            .semantics(mergeDescendants = true) { contentDescription = rowDescription },
        horizontalArrangement = Arrangement.spacedBy(GemaSpacing.rowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.widthIn(min = GemaSpacing.narrowCellWidth), contentAlignment = Alignment.CenterEnd) {
            GText(text = count.toString(), style = GTextStyle.NUMERAL)
        }
        GText(
            text = label,
            style = GTextStyle.BODY_LARGE,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LastBackupMessage(state: BackupUiState) {
    GText(text = stringResource(R.string.backup_last_title), style = GTextStyle.LABEL_SMALL)
    val days: Int? = state.daysSinceLastBackup
    if (days == null) {
        GText(text = stringResource(R.string.backup_last_never), style = GTextStyle.BODY_MEDIUM)
        return
    }
    val date: String = state.lastBackupDate?.asDayMonthYear().orEmpty()
    val elapsed: String = if (days == 0) {
        stringResource(R.string.backup_last_today)
    } else {
        pluralStringResource(R.plurals.backup_last_days_ago, days, days)
    }
    Row {
        GText(text = elapsed, modifier = Modifier.alignByBaseline(), style = GTextStyle.NUMERAL)
        GText(
            text = stringResource(R.string.backup_last_date_suffix, date),
            modifier = Modifier.alignByBaseline(),
            style = GTextStyle.BODY_MEDIUM,
        )
    }
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
