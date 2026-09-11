package com.emm.gema.core.domain.evaluation

enum class SummaryFormat(val extension: String, val mimeType: String) {
    CSV("csv", "text/csv"),
    PDF("pdf", "application/pdf"),
}
