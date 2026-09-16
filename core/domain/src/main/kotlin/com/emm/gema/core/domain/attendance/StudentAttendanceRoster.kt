package com.emm.gema.core.domain.attendance

import com.emm.gema.core.domain.student.Student
import java.time.LocalDate
import java.time.YearMonth

fun Student.attends(date: LocalDate): Boolean =
    withdrawalDate == null || date.isBefore(withdrawalDate)

fun Student.attendsMonth(month: YearMonth): Boolean = attends(month.atDay(1))
