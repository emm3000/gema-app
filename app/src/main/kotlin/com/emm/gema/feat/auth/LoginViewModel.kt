package com.emm.gema.feat.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.data.auth.AuthRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    var state by mutableStateOf(LoginUiState())
        private set

    init {
        combine(
            snapshotFlow { state.email },
            snapshotFlow { state.password },
        ) { email, password ->
            val isValidFields = email.isNotBlank() && password.isNotBlank()
            state = state.copy(isValidFields = isValidFields)
        }
            .launchIn(viewModelScope)
    }

    fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.Login -> login()
            is LoginAction.UpdateEmail -> state = state.copy(email = action.value)
            is LoginAction.UpdatePassword -> state = state.copy(password = action.value)
        }
    }

    private fun login() = viewModelScope.launch {
        tryLogin()
    }

    private suspend fun tryLogin() = try {
        state = state.copy(isLoading = true)
        authRepository.login(state.email, state.password)
        state = state.copy(successLogin = true)
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
        state = state.copy(errorMsg = e.message, isLoading = false)
    }
}