package com.emm.gema.core.domain.evaluation

import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.flow.first

class ExportPeriodLevelSummaryUseCase(
    private val getSummary: GetPeriodLevelSummaryUseCase,
    private val documents: SummaryDocuments,
    private val pdfRenderer: PeriodLevelSummaryPdfRenderer,
) {

    suspend operator fun invoke(
        sectionId: SectionId,
        periodId: PeriodId,
        sectionTitle: String,
        periodLabel: String,
        format: SummaryFormat,
    ): SummaryFile {
        val summary: PeriodLevelSummary = getSummary(sectionId, periodId).first()
        val bytes: ByteArray = when (format) {
            SummaryFormat.CSV -> PeriodLevelSummaryCsv.toCsv(summary).toByteArray(StandardCharsets.UTF_8)
            SummaryFormat.PDF -> pdfRenderer.render(sectionTitle, periodLabel, summary)
        }
        val fileName: String = periodLevelSummaryFileName(sectionTitle, periodLabel, format.extension)
        return documents.write(fileName, bytes)
    }
}
