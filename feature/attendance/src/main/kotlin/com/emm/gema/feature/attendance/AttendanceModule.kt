package com.emm.gema.feature.attendance

import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.feature.attendance.day.AttendanceDayViewModel
import com.emm.gema.feature.attendance.month.AttendanceMonthViewModel
import java.time.LocalDate
import java.time.YearMonth
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val attendanceModule: Module = module {
    viewModel { (sectionId: SectionId, date: LocalDate?) ->
        AttendanceDayViewModel(sectionId, date, get(), get(), get(), get())
    }
    viewModel { (sectionId: SectionId, month: YearMonth?) ->
        AttendanceMonthViewModel(sectionId, month, get(), get(), get(), get())
    }
}
