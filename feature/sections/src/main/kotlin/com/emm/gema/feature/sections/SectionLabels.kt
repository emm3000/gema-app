package com.emm.gema.feature.sections

import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section

fun Grade.label(): String = "$number°"

fun Section.title(): String = "${grade.label()} $name"

fun Area.label(): String = when (this) {
    Area.COMU -> "Comunicación"
    Area.CAST_SEGNL -> "Castellano como segunda lengua"
    Area.INGLES_EXT -> "Inglés como lengua extranjera"
    Area.MATE -> "Matemática"
    Area.CIENC_TEC -> "Ciencia y Tecnología"
    Area.PPSS -> "Personal Social"
    Area.EFIS -> "Educación Física"
    Area.ARTE -> "Arte y Cultura"
    Area.EREL -> "Educación Religiosa"
}
