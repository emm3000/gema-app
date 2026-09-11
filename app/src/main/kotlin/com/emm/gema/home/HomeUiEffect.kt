package com.emm.gema.home

sealed interface HomeUiEffect {

    data object NavigateToBackup : HomeUiEffect
}
