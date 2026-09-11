package com.emm.gema.navigation

import com.emm.gema.core.domain.section.Area

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
    const val STUDENTS: String = "students/{sectionId}"
    const val STUDENT_FORM: String = "student-form/{sectionId}?studentId={studentId}"
    const val WORKED_COMPETENCIES: String = "worked-competencies/{sectionId}/{periodId}/{area}"

    const val SCHOOL_YEAR_ID: String = "schoolYearId"
    const val SECTION_ID: String = "sectionId"
    const val STUDENT_ID: String = "studentId"
    const val PERIOD_ID: String = "periodId"
    const val AREA: String = "area"

    fun periodsOf(schoolYearId: String): String = "periods/$schoolYearId"

    fun sectionForm(schoolYearId: String, sectionId: String?): String =
        "section-form?schoolYearId=$schoolYearId&sectionId=${sectionId.orEmpty()}"

    fun sectionAreasOf(sectionId: String): String = "section-areas/$sectionId"

    fun sectionDetailOf(sectionId: String): String = "section-detail/$sectionId"

    fun studentsOf(sectionId: String): String = "students/$sectionId"

    fun studentForm(sectionId: String, studentId: String?): String =
        "student-form/$sectionId?studentId=${studentId.orEmpty()}"

    fun workedCompetenciesOf(sectionId: String, periodId: String, area: Area): String =
        "worked-competencies/$sectionId/$periodId/${area.name}"
}
