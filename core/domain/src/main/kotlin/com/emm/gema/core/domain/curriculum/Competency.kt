package com.emm.gema.core.domain.curriculum

import com.emm.gema.core.domain.section.Area

data class Competency(
    val id: CompetencyId,
    val area: Area,
    val siagieOrdinal: Int,
    val name: String,
) {
    init {
        require(siagieOrdinal >= FIRST_SIAGIE_ORDINAL) { "A competency is numbered from $FIRST_SIAGIE_ORDINAL" }
        require(name.isNotBlank()) { "A competency needs a name" }
    }

    companion object {
        const val FIRST_SIAGIE_ORDINAL: Int = 1

        fun idOf(area: Area, siagieOrdinal: Int): CompetencyId = CompetencyId("${area.name}-$siagieOrdinal")

        fun areaOf(id: CompetencyId): Area? = Area.entries.find { id.value.startsWith("${it.name}-") }
    }
}
