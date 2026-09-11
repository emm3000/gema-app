package com.emm.gema.core.database.student

import com.emm.gema.core.database.Student as StudentRow
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import java.time.LocalDate

fun StudentRow.toDomain(): Student = Student(
    id = id,
    sectionId = section_id,
    code = StudentCode(student_code),
    fullName = full_name,
    siagieId = siagie_id,
    withdrawalDate = withdrawal_date?.let(LocalDate::parse),
)
