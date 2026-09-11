package com.emm.gema.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.schoolyear.GetSchoolYearsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StartDestinationViewModel(
    private val getSchoolYears: GetSchoolYearsUseCase,
) : ViewModel() {

    private val _startDestination: MutableStateFlow<String?> = MutableStateFlow(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val hasSchoolYear: Boolean = getSchoolYears().first().isNotEmpty()
            _startDestination.value = if (hasSchoolYear) GemaRoutes.HOME else GemaRoutes.SETUP_YEAR
        }
    }
}
