package com.emm.gema.core.siagie

import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.siagie.SiagieGradeEntry
import com.emm.gema.core.domain.siagie.SiagieGradesWriter
import java.io.File
import java.nio.file.Files

private const val HEADER_ROW: Int = 3
private const val FIRST_DATA_ROW: Int = 4
private const val CODE_COLUMN: String = "B"
private const val ORDINAL_DIGITS: Int = 2
private const val LETTERS_IN_ALPHABET: Int = 26
private const val FIRST_LETTER: Char = 'A'

class XlsxSiagieGradesWriter : SiagieGradesWriter {

    override fun write(template: ByteArray, entries: List<SiagieGradeEntry>): ByteArray {
        val source: File = temporaryFile("source")
        val target: File = temporaryFile("target")
        try {
            source.writeBytes(template)
            val workbook = XlsxTemplate(source)
            workbook.fill(target, editsOf(workbook, entries))
            return target.readBytes()
        } finally {
            source.delete()
            target.delete()
        }
    }

    private fun editsOf(
        workbook: XlsxTemplate,
        entries: List<SiagieGradeEntry>,
    ): Map<String, Map<String, String>> {
        val sheetNames: List<String> = workbook.sheetNames()

        return entries
            .groupBy { sheetNameOf(it.area) }
            .filterKeys { it in sheetNames }
            .mapValues { (sheetName: String, sheetEntries: List<SiagieGradeEntry>) ->
                sheetEditsOf(workbook.readSheet(sheetName), sheetEntries)
            }
            .filterValues { it.isNotEmpty() }
    }

    private fun sheetEditsOf(
        cells: Map<String, String>,
        entries: List<SiagieGradeEntry>,
    ): Map<String, String> {
        val rows: Map<String, Int> = rowsByCode(cells)
        val columns: Map<Int, String> = columnsByOrdinal(cells)
        val edits: MutableMap<String, String> = LinkedHashMap()

        entries.forEach { entry: SiagieGradeEntry ->
            val row: Int? = rows[entry.studentCode.value]
            val column: String? = columns[entry.siagieOrdinal]
            if (row != null && column != null) {
                edits["$column$row"] = entry.achievementValue
                if (entry.descriptiveConclusion.isNotBlank()) {
                    edits["${nextColumn(column)}$row"] = entry.descriptiveConclusion
                }
            }
        }
        return edits
    }

    private fun rowsByCode(cells: Map<String, String>): Map<String, Int> = cells
        .mapNotNull { (reference: String, text: String) ->
            val row: Int = rowOf(reference)
            if (columnOf(reference) == CODE_COLUMN && row >= FIRST_DATA_ROW) text.trim() to row else null
        }
        .toMap()

    private fun columnsByOrdinal(cells: Map<String, String>): Map<Int, String> = cells
        .mapNotNull { (reference: String, text: String) ->
            val ordinal: Int? = ordinalOf(text)
            if (rowOf(reference) == HEADER_ROW && ordinal != null) ordinal to columnOf(reference) else null
        }
        .toMap()

    private fun ordinalOf(header: String): Int? = competencyHeader.find(header.trim())
        ?.groupValues
        ?.get(1)
        ?.toIntOrNull()

    private fun sheetNameOf(area: Area): String = area.name.replace('_', ' ')

    private fun columnOf(reference: String): String = reference.takeWhile(Char::isLetter)

    private fun rowOf(reference: String): Int = reference.dropWhile(Char::isLetter).toIntOrNull() ?: 0

    private fun nextColumn(column: String): String {
        val letters: CharArray = column.toCharArray()
        for (index: Int in letters.indices.reversed()) {
            if (letters[index] != FIRST_LETTER + LETTERS_IN_ALPHABET - 1) {
                letters[index] = letters[index] + 1
                return String(letters)
            }
            letters[index] = FIRST_LETTER
        }
        return FIRST_LETTER + String(letters)
    }

    private fun temporaryFile(prefix: String): File =
        Files.createTempFile("siagie-$prefix", ".xlsx").toFile()
}

private val competencyHeader: Regex = Regex("""Competencia\s+(\d{$ORDINAL_DIGITS})\s+NL""", RegexOption.IGNORE_CASE)
