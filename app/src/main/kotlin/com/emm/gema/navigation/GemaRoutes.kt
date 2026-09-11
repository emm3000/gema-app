package com.emm.gema.navigation

import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId
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
    const val ACTIVITIES: String = "activities/{sectionId}"
    const val ACTIVITY_FORM: String = "activity-form/{sectionId}?activityId={activityId}"
    const val ACTIVITY_EVIDENCE: String = "activity-evidence/{activityId}"

    const val SCHOOL_YEAR_ID: String = "schoolYearId"
    const val SECTION_ID: String = "sectionId"
    const val STUDENT_ID: String = "studentId"
    const val PERIOD_ID: String = "periodId"
    const val COMPETENCY_ID: String = "competencyId"
    const val ACTIVITY_ID: String = "activityId"
    const val URI: String = "uri"
    const val AREA: String = "area"
    const val DATE: String = "date"
    const val MONTH: String = "month"

    fun periodsOf(schoolYearId: SchoolYearId): String = "periods/${schoolYearId.value}"

    fun sectionForm(schoolYearId: SchoolYearId, sectionId: SectionId?): String =
        "section-form?schoolYearId=${schoolYearId.value}&sectionId=${sectionId?.value.orEmpty()}"

    fun sectionAreasOf(sectionId: SectionId): String = "section-areas/${sectionId.value}"

    fun sectionDetailOf(sectionId: SectionId): String = "section-detail/${sectionId.value}"

    fun attendanceDayOf(sectionId: SectionId, date: LocalDate?): String =
        "attendance/${sectionId.value}?date=${date?.toString().orEmpty()}"

    fun attendanceMonthOf(sectionId: SectionId, month: YearMonth?): String =
        "attendance-month/${sectionId.value}?month=${month?.toString().orEmpty()}"

    fun studentsOf(sectionId: SectionId): String = "students/${sectionId.value}"

    fun studentForm(sectionId: SectionId, studentId: StudentId?): String =
        "student-form/${sectionId.value}?studentId=${studentId?.value.orEmpty()}"

    fun importPreview(sectionId: SectionId, uri: String): String =
        "import-preview/${sectionId.value}/${encodeArgument(uri)}"
    fun periodLevelsOf(sectionId: SectionId): String = periodLevelCellOf(sectionId, null, null)

    fun periodLevelCellOf(sectionId: SectionId, studentId: StudentId?, competencyId: CompetencyId?): String =
        "period-levels/${sectionId.value}" +
            "?studentId=${studentId?.value.orEmpty()}" +
            "&competencyId=${competencyId?.value.orEmpty()}"

    fun exportOf(sectionId: SectionId): String = "export/${sectionId.value}"

    fun workedCompetenciesOf(sectionId: SectionId, periodId: PeriodId, area: Area): String =
        "worked-competencies/${sectionId.value}/${periodId.value}/${area.name}"

    fun activitiesOf(sectionId: SectionId): String = "activities/${sectionId.value}"

    fun activityForm(sectionId: SectionId, activityId: ActivityId?): String =
        "activity-form/${sectionId.value}?activityId=${activityId?.value.orEmpty()}"

    fun activityEvidenceOf(activityId: ActivityId): String = "activity-evidence/${activityId.value}"
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
