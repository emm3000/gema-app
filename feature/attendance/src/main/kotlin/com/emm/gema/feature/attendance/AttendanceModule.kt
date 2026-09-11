package com.emm.gema.feature.attendance

import com.emm.gema.feature.attendance.day.AttendanceDayViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import java.time.LocalDate

val attendanceModule: Module = module {
    viewModel { (sectionId: String, date: LocalDate?) ->
        AttendanceDayViewModel(sectionId, date, get(), get(), get(), get())
    }
}
