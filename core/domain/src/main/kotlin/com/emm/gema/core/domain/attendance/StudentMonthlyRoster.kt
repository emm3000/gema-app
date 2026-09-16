package com.emm.gema.core.domain.attendance

import com.emm.gema.core.domain.student.Student
import java.time.YearMonth

fun Student.attendsMonth(month: YearMonth): Boolean =
    withdrawalDate == null || withdrawalDate.isAfter(month.atDay(1))
