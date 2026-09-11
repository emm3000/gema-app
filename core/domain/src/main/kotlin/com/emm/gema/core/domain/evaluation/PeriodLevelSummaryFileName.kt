package com.emm.gema.core.domain.evaluation

import java.text.Normalizer

fun periodLevelSummaryFileName(sectionTitle: String, periodLabel: String, extension: String): String =
    "resumen-${slug(sectionTitle)}-${slug(periodLabel)}.$extension"

private fun slug(value: String): String {
    val normalized: String = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}"), "")
    return normalized.lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
}
