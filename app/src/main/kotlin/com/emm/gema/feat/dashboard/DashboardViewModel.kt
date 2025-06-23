package com.emm.gema.feat.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.domain.dashboard.Dashboard
import com.emm.gema.domain.dashboard.DashboardFetcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(dashboardFetcher: DashboardFetcher): ViewModel() {

    val state: StateFlow<Dashboard> = dashboardFetcher
        .fetch()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Dashboard.Empty
        )
}