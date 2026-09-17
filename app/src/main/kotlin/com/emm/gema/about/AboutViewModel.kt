package com.emm.gema.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AboutViewModel(appVersion: AppVersionProvider) : ViewModel() {

    private val _state: MutableStateFlow<AboutUiState> =
        MutableStateFlow(AboutUiState(version = appVersion.versionName()))
    val state: StateFlow<AboutUiState> = _state.asStateFlow()

    private val _effects: Channel<AboutUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<AboutUiEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: AboutUiIntent) {
        when (intent) {
            is AboutUiIntent.SourceLinkClicked -> emit(AboutUiEffect.OpenUrl(intent.source.url))
            AboutUiIntent.BackClicked -> emit(AboutUiEffect.NavigateBack)
        }
    }

    private fun emit(effect: AboutUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
