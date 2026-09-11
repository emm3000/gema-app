package com.emm.gema.core.domain.curriculum

import com.emm.gema.core.domain.section.Area

object PrimaryCurriculum {

    const val VERSION: Int = 1

    val competencies: List<Competency> = Area.entries.flatMap(::competenciesOf)

    fun of(area: Area): List<Competency> = competencies.filter { it.area == area }
}

private fun competenciesOf(area: Area): List<Competency> = namesOf(area).mapIndexed { index, name ->
    Competency(
        id = Competency.idOf(area, index + Competency.FIRST_SIAGIE_ORDINAL),
        area = area,
        siagieOrdinal = index + Competency.FIRST_SIAGIE_ORDINAL,
        name = name,
    )
}

private fun namesOf(area: Area): List<String> = when (area) {
    Area.COMU -> listOf(
        "Se comunica oralmente en lengua materna",
        "Lee diversos tipos de textos escritos",
        "Escribe diversos tipos de textos",
    )

    Area.CAST_SEGNL -> listOf(
        "Se comunica oralmente en Castellano como segunda lengua",
        "Lee diversos tipos de textos escritos en Castellano como segunda lengua",
        "Escribe diversos tipos de textos Castellano como segunda lengua",
    )

    Area.INGLES_EXT -> listOf(
        "Se comunica oralmente en Inglés como lengua extranjera",
        "Lee diversos tipos de textos en Inglés como lengua extranjera",
        "Escribe diversos tipos de textos Inglés como lengua extranjera",
    )

    Area.MATE -> listOf(
        "Resuelve problemas de cantidad",
        "Resuelve problemas de regularidad, equivalencia y cambio",
        "Resuelve problemas de forma, movimiento y localización",
        "Resuelve problemas de gestión de datos e incertidumbre",
    )

    Area.CIENC_TEC -> listOf(
        "Indaga mediante métodos científicos para construir sus conocimientos",
        "Explica el mundo físico basándose en conocimientos sobre los seres vivos, materia y energía, " +
            "biodiversidad, Tierra y universo",
        "Diseña y construye soluciones tecnológicas para resolver problemas de su entorno",
    )

    Area.PPSS -> listOf(
        "Construye su identidad",
        "Convive y participa democráticamente",
        "Construye interpretaciones históricas",
        "Gestiona responsablemente el espacio y el ambiente",
        "Gestiona responsablemente los recursos económicos",
    )

    Area.EFIS -> listOf(
        "Se desenvuelve de manera autónoma a través de su motricidad",
        "Asume una vida saludable",
        "Interactúa a través de sus habilidades sociomotrices",
    )

    Area.ARTE -> listOf(
        "Aprecia de manera crítica manifestaciones artístico-culturales",
        "Crea proyectos desde los lenguajes artísticos",
    )

    Area.EREL -> listOf(
        "Construye su identidad como persona humana, amada por Dios, digna, libre y trascendente, " +
            "comprendiendo la doctrina de su propia religión, abierto al diálogo con las que le son cercanas",
        "Asume la experiencia del encuentro personal y comunitario con Dios en su proyecto de vida en " +
            "coherencia con su creencia religiosa",
    )
}
