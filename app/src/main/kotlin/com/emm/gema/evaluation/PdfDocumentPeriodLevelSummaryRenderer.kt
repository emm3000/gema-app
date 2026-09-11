package com.emm.gema.evaluation

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelAreaSummary
import com.emm.gema.core.domain.evaluation.PeriodLevelGrid
import com.emm.gema.core.domain.evaluation.PeriodLevelGridRow
import com.emm.gema.core.domain.evaluation.PeriodLevelSummary
import com.emm.gema.core.domain.evaluation.PeriodLevelSummaryPdfRenderer
import java.io.ByteArrayOutputStream

private const val PAGE_WIDTH_POINTS: Int = 595
private const val PAGE_HEIGHT_POINTS: Int = 842
private const val MARGIN_POINTS: Float = 32f
private const val TITLE_TEXT_SIZE: Float = 14f
private const val AREA_TITLE_TEXT_SIZE: Float = 12f
private const val HEADER_TEXT_SIZE: Float = 9f
private const val CELL_TEXT_SIZE: Float = 9f
private const val ROW_HEIGHT_POINTS: Float = 20f
private const val STUDENT_COLUMN_WIDTH_POINTS: Float = 160f
private const val MINIMUM_COLUMN_WIDTH_POINTS: Float = 60f
private const val NAME_COLUMN_MAX_CHARS: Int = 26
private const val HEADER_COLUMN_MAX_CHARS: Int = 18
private const val CONCLUSIONS_TITLE: String = "Conclusiones descriptivas"

class PdfDocumentPeriodLevelSummaryRenderer : PeriodLevelSummaryPdfRenderer {

    override fun render(sectionTitle: String, periodLabel: String, summary: PeriodLevelSummary): ByteArray {
        val document = PdfDocument()
        val writer = PageWriter(document, sectionTitle, periodLabel)

        if (summary.areas.isEmpty()) {
            writer.startArea("")
        } else {
            summary.areas.forEach { areaSummary: PeriodLevelAreaSummary -> writer.drawArea(areaSummary) }
        }
        writer.finish()

        val output = ByteArrayOutputStream()
        document.writeTo(output)
        document.close()
        return output.toByteArray()
    }

    private class PageWriter(
        private val document: PdfDocument,
        private val sectionTitle: String,
        private val periodLabel: String,
    ) {
        private val titlePaint: Paint = textPaint(TITLE_TEXT_SIZE, isBold = true)
        private val areaTitlePaint: Paint = textPaint(AREA_TITLE_TEXT_SIZE, isBold = true)
        private val headerPaint: Paint = textPaint(HEADER_TEXT_SIZE, isBold = true)
        private val cellPaint: Paint = textPaint(CELL_TEXT_SIZE, isBold = false)

        private var page: PdfDocument.Page? = null
        private var canvas: Canvas? = null
        private var y: Float = MARGIN_POINTS

        fun drawArea(areaSummary: PeriodLevelAreaSummary) {
            startArea(areaSummary.area.officialName)
            val grid: PeriodLevelGrid = areaSummary.grid
            val columnWidth: Float = columnWidthOf(grid.columns)
            drawHeaderRow(grid.columns, columnWidth)
            grid.rows.forEach { row: PeriodLevelGridRow ->
                ensureSpace()
                drawRow(row, columnWidth)
            }
            drawConclusions(grid)
        }

        fun startArea(areaName: String) {
            newPage()
            drawText("$sectionTitle - $periodLabel", MARGIN_POINTS, titlePaint)
            advance(TITLE_TEXT_SIZE + ROW_HEIGHT_POINTS)
            if (areaName.isNotEmpty()) {
                drawText(areaName, MARGIN_POINTS, areaTitlePaint)
                advance(AREA_TITLE_TEXT_SIZE + ROW_HEIGHT_POINTS)
            }
        }

        fun finish() {
            page?.let { document.finishPage(it) }
        }

        private fun drawHeaderRow(columns: List<Competency>, columnWidth: Float) {
            drawText("Estudiante", MARGIN_POINTS, headerPaint)
            columns.forEachIndexed { index: Int, column: Competency ->
                val label: String = "${column.siagieOrdinal}. ${column.name}".take(HEADER_COLUMN_MAX_CHARS)
                drawText(label, MARGIN_POINTS + STUDENT_COLUMN_WIDTH_POINTS + index * columnWidth, headerPaint)
            }
            advance(ROW_HEIGHT_POINTS)
        }

        private fun drawRow(row: PeriodLevelGridRow, columnWidth: Float) {
            val name: String = row.student.fullName.take(NAME_COLUMN_MAX_CHARS)
            drawText(name, MARGIN_POINTS, cellPaint)
            row.cells.forEachIndexed { index: Int, cell: PeriodLevel ->
                val value: String = cell.achievementLevel?.name ?: cell.unworkedComment?.label.orEmpty()
                drawText(value, MARGIN_POINTS + STUDENT_COLUMN_WIDTH_POINTS + index * columnWidth, cellPaint)
            }
            advance(ROW_HEIGHT_POINTS)
        }

        private fun drawConclusions(grid: PeriodLevelGrid) {
            val entries: List<Pair<PeriodLevelGridRow, PeriodLevel>> = grid.rows.flatMap { row ->
                row.cells
                    .filter { it.achievementLevel == AchievementLevel.C && it.descriptiveConclusion.isNotBlank() }
                    .map { row to it }
            }
            if (entries.isEmpty()) return

            ensureSpace()
            advance(ROW_HEIGHT_POINTS / 2)
            ensureSpace()
            drawText(CONCLUSIONS_TITLE, MARGIN_POINTS, headerPaint)
            advance(ROW_HEIGHT_POINTS)

            entries.forEach { (row, cell) ->
                ensureSpace()
                val competency: Competency? = grid.columns.find { it.id == cell.key.competencyId }
                val competencyName: String = competency?.name.orEmpty()
                val label: String = "${row.student.fullName} - $competencyName: ${cell.descriptiveConclusion}"
                drawText(label, MARGIN_POINTS, cellPaint)
                advance(ROW_HEIGHT_POINTS)
            }
        }

        private fun newPage() {
            page?.let { document.finishPage(it) }
            val info: PdfDocument.PageInfo =
                PdfDocument.PageInfo.Builder(PAGE_WIDTH_POINTS, PAGE_HEIGHT_POINTS, document.pages.size + 1).create()
            val startedPage: PdfDocument.Page = document.startPage(info)
            page = startedPage
            canvas = startedPage.canvas
            y = MARGIN_POINTS
        }

        private fun ensureSpace() {
            if (y + ROW_HEIGHT_POINTS > PAGE_HEIGHT_POINTS - MARGIN_POINTS) {
                newPage()
            }
        }

        private fun drawText(text: String, x: Float, paint: Paint) {
            canvas?.drawText(text, x, y, paint)
        }

        private fun advance(amount: Float) {
            y += amount
        }
    }
}

private fun columnWidthOf(columns: List<Competency>): Float {
    val available: Float = PAGE_WIDTH_POINTS - MARGIN_POINTS * 2 - STUDENT_COLUMN_WIDTH_POINTS
    if (columns.isEmpty()) return available
    return maxOf(available / columns.size, MINIMUM_COLUMN_WIDTH_POINTS)
}

private fun textPaint(size: Float, isBold: Boolean): Paint = Paint().apply {
    textSize = size
    isAntiAlias = true
    isFakeBoldText = isBold
}
