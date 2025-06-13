@file:Suppress("OPT_IN_USAGE")

package com.emm.gema.feat.auth

import android.util.Log
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

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {

    var state by mutableStateOf(RegisterUiState())
        private set

    init {
        combine(
            flow = snapshotFlow { state.email },
            flow2 = snapshotFlow { state.password },
            flow3 = snapshotFlow { state.confirmPassword },
            flow4 = snapshotFlow { state.isChecked },
            transform = ::validateSignUpFields,
        )
            .launchIn(viewModelScope)
    }

    fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.OnNameChange -> state = state.copy(name = action.value)
            is RegisterAction.OnEmailChange -> state = state.copy(email = action.value)
            is RegisterAction.OnPasswordChange -> state = state.copy(password = action.value)
            is RegisterAction.OnConfirmPasswordChange -> state = state.copy(confirmPassword = action.value)
            is RegisterAction.OnCheckedChange -> state = state.copy(isChecked = action.value)
            RegisterAction.Register -> executeSignUp()
        }
    }

    private fun executeSignUp() = viewModelScope.launch {
        try {
            state = state.copy(isLoading = true)
            repository.register(
                name = state.email,
                email = state.password,
                password = state.email
            )
            state = state.copy(success = true)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            state = state.copy(error = e.stackTraceToString(), isLoading = false)
        }
    }

    private fun validateSignUpFields(
        email: String,
        password: String,
        confirmPassword: String,
        isChecked: Boolean,
    ) {
        val isValidPassword = password.length >= 6
        val isValidConfirmPassword = password == confirmPassword
        val isValidFields: Boolean = isValidFields(
            email = email,
            isValidPassword = isValidPassword,
            isValidConfirmPassword = isValidConfirmPassword,
            isChecked = isChecked,
        )
        Log.e("aea", isValidFields.toString())
        state = state.copy(isValidFields = isValidFields)
    }

    private fun isValidFields(
        email: String,
        isValidPassword: Boolean,
        isValidConfirmPassword: Boolean,
        isChecked: Boolean,
    ) = isValidPassword
            && isValidConfirmPassword
            && isChecked
            && email.isNotBlank()
}