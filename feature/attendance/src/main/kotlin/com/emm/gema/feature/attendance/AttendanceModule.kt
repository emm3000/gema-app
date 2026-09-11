package com.emm.gema.feature.attendance

import com.emm.gema.feature.attendance.day.AttendanceDayViewModel
import com.emm.gema.feature.attendance.month.AttendanceMonthViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import java.time.LocalDate
import java.time.YearMonth

val attendanceModule: Module = module {
    viewModel { (sectionId: String, date: LocalDate?) ->
        AttendanceDayViewModel(sectionId, date, get(), get(), get(), get())
    }
    viewModel { (sectionId: String, month: YearMonth?) ->
        AttendanceMonthViewModel(sectionId, month, get(), get(), get(), get())
    }
}
