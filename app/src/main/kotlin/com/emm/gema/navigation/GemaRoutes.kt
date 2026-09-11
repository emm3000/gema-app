package com.emm.gema.navigation

object GemaRoutes {
    const val HOME: String = "home"
    const val BACKUP: String = "backup"
    const val SETUP_YEAR: String = "setup/year"
    const val SETUP_SECTION: String = "setup/section"
    const val SCHOOL_YEARS: String = "school-years"
    const val PERIODS: String = "periods/{schoolYearId}"
    const val SECTION_FORM: String = "section-form?schoolYearId={schoolYearId}&sectionId={sectionId}"
    const val SECTION_AREAS: String = "section-areas/{sectionId}"

    const val SCHOOL_YEAR_ID: String = "schoolYearId"
    const val SECTION_ID: String = "sectionId"

    fun periodsOf(schoolYearId: String): String = "periods/$schoolYearId"

    fun sectionForm(schoolYearId: String, sectionId: String?): String =
        "section-form?schoolYearId=$schoolYearId&sectionId=${sectionId.orEmpty()}"

    fun sectionAreasOf(sectionId: String): String = "section-areas/$sectionId"
}
