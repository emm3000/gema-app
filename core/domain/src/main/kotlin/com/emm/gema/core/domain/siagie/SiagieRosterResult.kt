package com.emm.gema.core.domain.siagie

sealed interface SiagieRosterResult {

    data class Parsed(val roster: SiagieRoster) : SiagieRosterResult

    data class Malformed(val row: Int) : SiagieRosterResult

    data object NotASiagieTemplate : SiagieRosterResult
}
