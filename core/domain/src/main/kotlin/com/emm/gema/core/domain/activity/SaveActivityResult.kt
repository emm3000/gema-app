package com.emm.gema.core.domain.activity

sealed interface SaveActivityResult {

    data class Saved(val activity: Activity) : SaveActivityResult

    data object DateOutsidePeriods : SaveActivityResult
}
