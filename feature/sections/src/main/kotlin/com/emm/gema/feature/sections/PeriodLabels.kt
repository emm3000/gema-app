package com.emm.gema.feature.sections

import com.emm.gema.core.domain.schoolyear.PeriodKind

private val romanOrdinals: List<String> = listOf("I", "II", "III", "IV")

fun PeriodKind.labelFor(ordinal: Int): String = "${romanOrdinals[ordinal - 1]} ${kindLabel()}"

fun PeriodKind.kindLabel(): String = when (this) {
    PeriodKind.BIMESTER -> "Bimestre"
    PeriodKind.TRIMESTER -> "Trimestre"
}
