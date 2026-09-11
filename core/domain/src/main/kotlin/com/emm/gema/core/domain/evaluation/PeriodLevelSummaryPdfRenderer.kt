package com.emm.gema.core.domain.evaluation

interface PeriodLevelSummaryPdfRenderer {

    fun render(sectionTitle: String, periodLabel: String, summary: PeriodLevelSummary): ByteArray
}
