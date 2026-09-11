package com.emm.gema.feature.students

import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.feature.students.form.StudentFormViewModel
import com.emm.gema.feature.students.list.StudentsViewModel
import com.emm.gema.feature.students.siagie.ImportPreviewViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val studentsModule: Module = module {
    viewModel { (sectionId: SectionId) -> StudentsViewModel(sectionId, get(), get(), get()) }
    viewModel { (sectionId: SectionId, studentId: StudentId?) ->
        StudentFormViewModel(sectionId, studentId, get(), get(), get(), get(), get())
    }
    viewModel { (sectionId: SectionId, uri: String) ->
        ImportPreviewViewModel(sectionId, uri, get(), get(), get(), get())
    }
}
