package com.emm.gema.core.domain.evaluation

enum class UnworkedComment(val siagieOrdinal: Int, val label: String, val officialText: String) {
    NO_ACTIONS(
        1,
        "No se realizaron acciones",
        "Comentario 1. No se logró realizar acciones para su desarrollo",
    ),
    NOT_ENOUGH_EVIDENCE(
        2,
        "Evidencia insuficiente",
        "Comentario 2. No se cuenta con evidencia suficiente para determinar nivel de logro.",
    ),
    OTHER(3, "Otro", "Comentario 3. Otro.");

    val siagieValue: String get() = "Comentario $siagieOrdinal"
}
