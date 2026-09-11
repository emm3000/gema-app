package com.emm.gema.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.backup.BackupStatus
import com.emm.gema.core.domain.backup.ObserveBackupStatusUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val observeBackupStatus: ObserveBackupStatusUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _effects: Channel<HomeUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<HomeUiEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeBackupStatus().collect(::onBackupStatus)
        }
    }

    fun onIntent(intent: HomeUiIntent) {
        when (intent) {
            HomeUiIntent.BackupReminderClicked -> viewModelScope.launch {
                _effects.send(HomeUiEffect.NavigateToBackup)
            }
        }
    }

    private fun onBackupStatus(status: BackupStatus) {
        _state.value = _state.value.copy(
            isLoading = false,
            backupReminder = reminderOf(status),
        )
    }

    private fun reminderOf(status: BackupStatus): BackupReminder? {
        if (!status.isReminderDue) return null
        return BackupReminder(
            daysSinceLastBackup = status.daysSinceLastBackup ?: 0,
            hasEverBackedUp = status.lastBackupAt != null,
        )
    }
}
