package com.emm.gema.navigation

import com.emm.gema.core.domain.section.Area
import java.time.LocalDate
import java.time.YearMonth

object GemaRoutes {
    const val HOME: String = "home"
    const val BACKUP: String = "backup"
    const val SETUP_YEAR: String = "setup/year"
    const val SETUP_SECTION: String = "setup/section"
    const val SCHOOL_YEARS: String = "school-years"
    const val PERIODS: String = "periods/{schoolYearId}"
    const val SECTION_FORM: String = "section-form?schoolYearId={schoolYearId}&sectionId={sectionId}"
    const val SECTION_AREAS: String = "section-areas/{sectionId}"
    const val SECTION_DETAIL: String = "section-detail/{sectionId}"
    const val ATTENDANCE_DAY: String = "attendance/{sectionId}?date={date}"
    const val ATTENDANCE_MONTH: String = "attendance-month/{sectionId}?month={month}"
    const val STUDENTS: String = "students/{sectionId}"
    const val STUDENT_FORM: String = "student-form/{sectionId}?studentId={studentId}"
    const val IMPORT_PREVIEW: String = "import-preview/{sectionId}/{uri}"
    const val PERIOD_LEVELS: String =
        "period-levels/{sectionId}?studentId={studentId}&competencyId={competencyId}"
    const val EXPORT: String = "export/{sectionId}"
    const val WORKED_COMPETENCIES: String = "worked-competencies/{sectionId}/{periodId}/{area}"

    const val SCHOOL_YEAR_ID: String = "schoolYearId"
    const val SECTION_ID: String = "sectionId"
    const val STUDENT_ID: String = "studentId"
    const val PERIOD_ID: String = "periodId"
    const val COMPETENCY_ID: String = "competencyId"
    const val URI: String = "uri"
    const val AREA: String = "area"
    const val DATE: String = "date"
    const val MONTH: String = "month"

    fun periodsOf(schoolYearId: String): String = "periods/$schoolYearId"

    fun sectionForm(schoolYearId: String, sectionId: String?): String =
        "section-form?schoolYearId=$schoolYearId&sectionId=${sectionId.orEmpty()}"

    fun sectionAreasOf(sectionId: String): String = "section-areas/$sectionId"

    fun sectionDetailOf(sectionId: String): String = "section-detail/$sectionId"

    fun attendanceDayOf(sectionId: String, date: LocalDate?): String =
        "attendance/$sectionId?date=${date?.toString().orEmpty()}"

    fun attendanceMonthOf(sectionId: String, month: YearMonth?): String =
        "attendance-month/$sectionId?month=${month?.toString().orEmpty()}"

    fun studentsOf(sectionId: String): String = "students/$sectionId"

    fun studentForm(sectionId: String, studentId: String?): String =
        "student-form/$sectionId?studentId=${studentId.orEmpty()}"

    fun importPreview(sectionId: String, uri: String): String =
        "import-preview/$sectionId/${encodeArgument(uri)}"
    fun periodLevelsOf(sectionId: String): String = periodLevelCellOf(sectionId, null, null)

    fun periodLevelCellOf(sectionId: String, studentId: String?, competencyId: String?): String =
        "period-levels/$sectionId?studentId=${studentId.orEmpty()}&competencyId=${competencyId.orEmpty()}"

    fun exportOf(sectionId: String): String = "export/$sectionId"

    fun workedCompetenciesOf(sectionId: String, periodId: String, area: Area): String =
        "worked-competencies/$sectionId/$periodId/${area.name}"
}

private const val UNRESERVED: String = "-._~"
private const val BYTE_MASK: Int = 0xFF
private const val ASCII_LIMIT: Int = 0x80
private const val HEXADECIMAL: Int = 16
private const val HEXADECIMAL_DIGITS: Int = 2

private fun encodeArgument(value: String): String = buildString {
    value.toByteArray(Charsets.UTF_8).forEach { byte: Byte ->
        val code: Int = byte.toInt() and BYTE_MASK
        val character: Char = code.toChar()
        if (character.isLetterOrDigit() && code < ASCII_LIMIT || character in UNRESERVED) {
            append(character)
        } else {
            append('%')
            append(code.toString(radix = HEXADECIMAL).uppercase().padStart(HEXADECIMAL_DIGITS, '0'))
        }
    }
}
