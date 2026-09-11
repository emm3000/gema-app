package com.emm.gema.core.domain.evaluation

class FakePeriodLevelSummaryPdfRenderer : PeriodLevelSummaryPdfRenderer {

    override fun render(sectionTitle: String, periodLabel: String, summary: PeriodLevelSummary): ByteArray =
        RENDERED_BYTES

    companion object {
        val RENDERED_BYTES: ByteArray = byteArrayOf(1, 2, 3)
    }
}
