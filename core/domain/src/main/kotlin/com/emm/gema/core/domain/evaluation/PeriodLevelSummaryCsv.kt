package com.emm.gema.core.domain.evaluation

import com.emm.gema.core.domain.curriculum.Competency

object PeriodLevelSummaryCsv {

    const val BOM: String = "\uFEFF"
    private const val STUDENT_HEADER: String = "Estudiante"
    private const val LINE_ENDING: String = "\r\n"

    fun toCsv(summary: PeriodLevelSummary): String {
        val blocks: List<String> = summary.areas.map { areaBlock(it) }
        return BOM + blocks.joinToString(LINE_ENDING + LINE_ENDING)
    }

    private fun areaBlock(areaSummary: PeriodLevelAreaSummary): String {
        val titleRow: String = escape(areaSummary.area.officialName)
        val header: String = (listOf(STUDENT_HEADER) + areaSummary.grid.columns.map { it.headerLabel })
            .joinToString(",") { escape(it) }
        val rows: List<String> = areaSummary.grid.rows.map { row ->
            (listOf(row.student.fullName) + row.cells.map { it.cellValue }).joinToString(",") { escape(it) }
        }
        return (listOf(titleRow, header) + rows).joinToString(LINE_ENDING)
    }

    private val Competency.headerLabel: String
        get() = "${siagieOrdinal.toString().padStart(2, '0')} - $name"

    private val PeriodLevel.cellValue: String
        get() = when {
            achievementLevel == AchievementLevel.C && descriptiveConclusion.isNotBlank() ->
                "${AchievementLevel.C.name}: $descriptiveConclusion"

            achievementLevel != null -> achievementLevel.name
            else -> unworkedComment?.label ?: ""
        }

    private val specialChars: Set<Char> = setOf(',', '"', '\n', '\r')

    private fun escape(value: String): String =
        if (value.any { it in specialChars }) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
}
