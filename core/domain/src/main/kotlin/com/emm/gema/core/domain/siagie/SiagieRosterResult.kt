package com.emm.gema.core.domain.siagie

sealed interface SiagieRosterResult {

    data class Parsed(val roster: SiagieRoster) : SiagieRosterResult

    data object NotASiagieTemplate : SiagieRosterResult
}
