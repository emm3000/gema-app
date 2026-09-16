package com.emm.gema.feature.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.backup.BACKUP_MIME_TYPE
import com.emm.gema.core.domain.backup.BackupFile
import com.emm.gema.core.domain.backup.BackupInspection
import com.emm.gema.core.domain.backup.BackupStatus
import com.emm.gema.core.domain.backup.BackupValidation
import com.emm.gema.core.domain.backup.CreateBackupUseCase
import com.emm.gema.core.domain.backup.InspectBackupUseCase
import com.emm.gema.core.domain.backup.ObserveBackupStatusUseCase
import com.emm.gema.core.domain.backup.ReminderThresholdResult
import com.emm.gema.core.domain.backup.RestoreBackupUseCase
import com.emm.gema.core.domain.backup.RestoreResult
import com.emm.gema.core.domain.backup.SetReminderThresholdUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearsUseCase
import com.emm.gema.core.domain.student.CountAllStudentsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Clock

private val restoreMimeTypes: List<String> = listOf(BACKUP_MIME_TYPE, "application/x-sqlite3", "*/*")

class BackupViewModel(
    private val observeBackupStatus: ObserveBackupStatusUseCase,
    private val createBackup: CreateBackupUseCase,
    private val inspectBackup: InspectBackupUseCase,
    private val restoreBackup: RestoreBackupUseCase,
    private val setReminderThreshold: SetReminderThresholdUseCase,
    private val getSchoolYears: GetSchoolYearsUseCase,
    private val countAllStudents: CountAllStudentsUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val _state: MutableStateFlow<BackupUiState> = MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state.asStateFlow()

    private val _effects: Channel<BackupUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<BackupUiEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeBackupStatus().collect(::onStatus)
        }
    }

    fun onIntent(intent: BackupUiIntent) {
        when (intent) {
            BackupUiIntent.CreateBackupClicked -> createBackupFile()
            BackupUiIntent.ChooseRestoreFileClicked -> openPicker()
            is BackupUiIntent.RestoreFilePicked -> inspectPickedFile(intent.uri)
            BackupUiIntent.RestoreConfirmed -> restoreConfirmedBackup()
            BackupUiIntent.RestoreDismissed -> dismissRestore()
            is BackupUiIntent.ReminderThresholdChanged -> changeReminderThreshold(intent.value)
            BackupUiIntent.BackClicked -> emit(BackupUiEffect.NavigateBack)
        }
    }

    private fun onStatus(status: BackupStatus) {
        val daysSinceLastBackup: Int? = status.daysSinceLastBackup
        _state.value = _state.value.copy(
            isLoading = false,
            lastBackupDate = status.lastBackupAt?.atZone(clock.zone)?.toLocalDate(),
            daysSinceLastBackup = daysSinceLastBackup,
            reminderThresholdDays = status.reminderThresholdDays,
            reminderThresholdInput = if (_state.value.isReminderThresholdInvalid) {
                _state.value.reminderThresholdInput
            } else {
                status.reminderThresholdDays.toString()
            },
            isBackupOverdue = status.lastBackupAt != null && status.isReminderDue,
        )
    }

    private fun createBackupFile() {
        if (_state.value.isCreating) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isCreating = true)
            val created: Result<BackupFile> = runCatching { createBackup() }
            _state.value = _state.value.copy(isCreating = false)
            created
                .onSuccess { file ->
                    emit(BackupUiEffect.ShareFile(path = file.path, mimeType = BACKUP_MIME_TYPE))
                    emit(BackupUiEffect.ShowMessage(BackupMessage.BACKUP_CREATED))
                }
                .onFailure { emit(BackupUiEffect.ShowMessage(BackupMessage.BACKUP_FAILED)) }
        }
    }

    private fun openPicker() {
        emit(BackupUiEffect.OpenDocumentPicker(restoreMimeTypes))
    }

    private fun inspectPickedFile(uri: String) {
        viewModelScope.launch {
            val inspection: BackupInspection = inspectBackup(uri)
            when (inspection.validation) {
                is BackupValidation.Valid -> askForConfirmation(uri, inspection.fileName)
                BackupValidation.NotABackup ->
                    emit(BackupUiEffect.ShowMessage(BackupMessage.FILE_IS_NOT_A_BACKUP))

                is BackupValidation.FromANewerApp ->
                    emit(BackupUiEffect.ShowMessage(BackupMessage.BACKUP_FROM_A_NEWER_APP))
            }
        }
    }

    private suspend fun askForConfirmation(uri: String, fileName: String) {
        _state.value = _state.value.copy(
            restoreConfirmation = RestoreConfirmation(
                uri = uri,
                fileName = fileName,
                currentSchoolYearCount = getSchoolYears().first().size,
                currentStudentCount = countAllStudents(),
            ),
        )
    }

    private fun restoreConfirmedBackup() {
        val confirmation: RestoreConfirmation = _state.value.restoreConfirmation ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isRestoring = true)
            val result: Result<RestoreResult> = runCatching { restoreBackup(confirmation.uri) }
            _state.value = _state.value.copy(isRestoring = false, restoreConfirmation = null)
            when (result.getOrNull()) {
                RestoreResult.Restored -> emit(BackupUiEffect.RestartApp)
                else -> emit(BackupUiEffect.ShowMessage(BackupMessage.RESTORE_FAILED))
            }
        }
    }

    private fun dismissRestore() {
        _state.value = _state.value.copy(restoreConfirmation = null)
    }

    private fun changeReminderThreshold(value: String) {
        val days: Int? = value.trim().toIntOrNull()
        _state.value = _state.value.copy(reminderThresholdInput = value)
        if (days == null) {
            _state.value = _state.value.copy(isReminderThresholdInvalid = true)
            return
        }
        viewModelScope.launch {
            val result: ReminderThresholdResult = setReminderThreshold(days)
            _state.value = _state.value.copy(
                isReminderThresholdInvalid = result !is ReminderThresholdResult.Saved,
            )
        }
    }

    private fun emit(effect: BackupUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
