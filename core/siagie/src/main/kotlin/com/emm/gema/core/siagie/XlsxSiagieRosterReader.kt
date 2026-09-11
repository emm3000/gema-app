package com.emm.gema.core.siagie

import com.emm.gema.core.domain.siagie.SiagieRoster
import com.emm.gema.core.domain.siagie.SiagieRosterReader
import com.emm.gema.core.domain.siagie.SiagieRosterResult
import com.emm.gema.core.domain.siagie.SiagieRosterStudent
import com.emm.gema.core.domain.student.StudentCode
import java.io.File
import java.nio.file.Files

private const val HEADER_ROW: Int = 3
private const val FIRST_DATA_ROW: Int = 4
private const val ID_HEADER: String = "ID"
private const val CODE_HEADER: String = "CodEstudiante"
private const val NAME_HEADER: String = "Nombres"

private val gradeInTitle: Regex = Regex("""(\d)\s*(?:ro|do|to|mo|vo|°)?\s*primaria""", RegexOption.IGNORE_CASE)
private val sectionInTitle: Regex = Regex("""secci[oó]n\s*:?\s*([A-Za-z0-9]+)""", RegexOption.IGNORE_CASE)

private sealed interface RosterRows {

    data class Students(val students: List<SiagieRosterStudent>) : RosterRows

    data class Malformed(val row: Int) : RosterRows
}

class XlsxSiagieRosterReader : SiagieRosterReader {

    override fun read(fileName: String, content: ByteArray): SiagieRosterResult =
        runCatching { rosterOf(fileName, content) }.getOrElse { SiagieRosterResult.NotASiagieTemplate }

    private fun rosterOf(fileName: String, content: ByteArray): SiagieRosterResult {
        val workbook: File = Files.createTempFile("siagie", ".xlsx").toFile()
        try {
            workbook.writeBytes(content)
            val template = XlsxTemplate(workbook)
            val sheetName: String = template.sheetNames().firstOrNull()
                ?: return SiagieRosterResult.NotASiagieTemplate
            val cells: Map<String, String> = template.readSheet(sheetName)
            if (!hasRosterHeader(cells)) return SiagieRosterResult.NotASiagieTemplate
            return when (val rows: RosterRows = rowsOf(cells)) {
                is RosterRows.Malformed -> SiagieRosterResult.Malformed(rows.row)
                is RosterRows.Students -> SiagieRosterResult.Parsed(rosterOf(cells, fileName, rows.students))
            }
        } finally {
            workbook.delete()
        }
    }

    private fun rosterOf(
        cells: Map<String, String>,
        fileName: String,
        students: List<SiagieRosterStudent>,
    ): SiagieRoster = SiagieRoster(
        gradeNumber = gradeNumberOf(cells, fileName),
        sectionName = sectionNameOf(cells),
        students = students,
    )

    private fun hasRosterHeader(cells: Map<String, String>): Boolean =
        cells["A$HEADER_ROW"] == ID_HEADER &&
            cells["B$HEADER_ROW"] == CODE_HEADER &&
            cells["C$HEADER_ROW"] == NAME_HEADER

    private fun gradeNumberOf(cells: Map<String, String>, fileName: String): Int? {
        val sources: List<String> = titlesOf(cells) + fileName
        return sources.firstNotNullOfOrNull { text ->
            gradeInTitle.find(text)?.groupValues?.get(1)?.toIntOrNull()
        }
    }

    private fun sectionNameOf(cells: Map<String, String>): String? = titlesOf(cells)
        .firstNotNullOfOrNull { text -> sectionInTitle.find(text)?.groupValues?.get(1) }

    private fun titlesOf(cells: Map<String, String>): List<String> = cells
        .filterKeys { reference -> rowOf(reference) < HEADER_ROW }
        .values
        .toList()

    private fun rowsOf(cells: Map<String, String>): RosterRows {
        val students: MutableList<SiagieRosterStudent> = mutableListOf()
        generateSequence(FIRST_DATA_ROW) { row: Int -> row + 1 }
            .takeWhile { row: Int -> isPopulated(cells, row) }
            .forEach { row: Int ->
                val student: SiagieRosterStudent = studentAt(cells, row)
                    ?: return RosterRows.Malformed(row)
                students.add(student)
            }
        return RosterRows.Students(students)
    }

    private fun isPopulated(cells: Map<String, String>, row: Int): Boolean =
        textOf(cells, "B$row") != null || textOf(cells, "C$row") != null

    private fun studentAt(cells: Map<String, String>, row: Int): SiagieRosterStudent? {
        val code: StudentCode = textOf(cells, "B$row")?.let(StudentCode::orNull) ?: return null
        val fullName: String = textOf(cells, "C$row") ?: return null
        return SiagieRosterStudent(textOf(cells, "A$row"), code, fullName)
    }

    private fun textOf(cells: Map<String, String>, reference: String): String? =
        cells[reference]?.trim()?.takeIf(String::isNotEmpty)

    private fun rowOf(reference: String): Int = reference.dropWhile(Char::isLetter).toIntOrNull() ?: 0
}
