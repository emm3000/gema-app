package com.emm.gema.core.database.section

import com.emm.gema.core.database.Section as SectionRow
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId

fun SectionRow.toDomain(): Section = Section(
    id = SectionId(id),
    schoolYearId = SchoolYearId(school_year_id),
    grade = Grade.valueOf(grade),
    name = name,
)
