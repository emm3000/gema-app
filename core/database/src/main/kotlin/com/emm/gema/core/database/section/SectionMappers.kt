package com.emm.gema.core.database.section

import com.emm.gema.core.database.Section as SectionRow
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section

fun SectionRow.toDomain(): Section = Section(
    id = id,
    schoolYearId = school_year_id,
    grade = Grade.valueOf(grade),
    name = name,
)
