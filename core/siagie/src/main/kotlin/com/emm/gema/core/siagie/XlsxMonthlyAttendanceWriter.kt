package com.emm.gema.core.siagie

import com.emm.gema.core.domain.siagie.AttendanceExportEntry
import com.emm.gema.core.domain.siagie.AttendanceExportFile
import com.emm.gema.core.domain.siagie.MonthlyAttendanceExporter
import com.emm.gema.core.domain.siagie.SiagieDocuments
import java.io.File
import java.time.YearMonth

private const val DATA_SHEET_INDEX: Int = 1
private const val FIRST_DAY_COLUMN_INDEX: Int = 4
private const val FIRST_DATA_ROW: Int = 4
private const val CODE_COLUMN: String = "B"
private const val LETTERS_IN_ALPHABET: Int = 26

class XlsxMonthlyAttendanceWriter(
    private val documents: SiagieDocuments,
    private val exportDirectory: File,
) : MonthlyAttendanceExporter {

    override suspend fun export(
        templateUri: String,
        month: YearMonth,
        entries: List<AttendanceExportEntry>,
    ): AttendanceExportFile {
        val fileName: String = documents.nameOf(templateUri)
        val content: ByteArray = documents.readContent(templateUri)
        val source: File = File.createTempFile("attendance-template", ".xlsx")
        return try {
            source.writeBytes(content)
            val template = XlsxTemplate(source)
            val sheetName: String = template.sheetNames()[DATA_SHEET_INDEX]
            val rowsByCode: Map<String, Int> = rowsByCode(template.readSheet(sheetName))
            val edits: Map<String, String> = editsOf(rowsByCode, month, entries)

            exportDirectory.mkdirs()
            val target = File(exportDirectory, fileName)
            template.fill(target, mapOf(sheetName to edits))
            AttendanceExportFile(fileName = fileName, path = target.absolutePath)
        } finally {
            source.delete()
        }
    }

    private fun editsOf(
        rowsByCode: Map<String, Int>,
        month: YearMonth,
        entries: List<AttendanceExportEntry>,
    ): Map<String, String> {
        val edits: MutableMap<String, String> = LinkedHashMap()
        entries.forEach { entry: AttendanceExportEntry ->
            val row: Int = rowsByCode[entry.studentCode.value] ?: return@forEach
            entry.statusesByDate.forEach { (date, status) ->
                if (YearMonth.from(date) != month) return@forEach
                val column: String = columnLetter(FIRST_DAY_COLUMN_INDEX + date.dayOfMonth - 1)
                edits["$column$row"] = AttendanceSiagieCode.of(status)
            }
        }
        return edits
    }

    private fun rowsByCode(cells: Map<String, String>): Map<String, Int> {
        val rows: MutableMap<String, Int> = LinkedHashMap()
        var row: Int = FIRST_DATA_ROW
        while (true) {
            val code: String = cells["$CODE_COLUMN$row"] ?: break
            rows[code] = row
            row++
        }
        return rows
    }

    private fun columnLetter(index: Int): String {
        var remaining: Int = index
        val letters = StringBuilder()
        while (remaining > 0) {
            val digit: Int = (remaining - 1) % LETTERS_IN_ALPHABET
            letters.insert(0, 'A' + digit)
            remaining = (remaining - 1) / LETTERS_IN_ALPHABET
        }
        return letters.toString()
    }
}
