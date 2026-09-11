package com.emm.gema.feature.sections.areas

import com.emm.gema.core.domain.section.Area

sealed interface SectionAreasUiIntent {

    data class AreaToggled(val id: Area, val isActive: Boolean) : SectionAreasUiIntent

    data object BackClicked : SectionAreasUiIntent
}
