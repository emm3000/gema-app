package com.emm.gema.core.domain.student

import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearRepository
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionRepository
import kotlinx.coroutines.flow.first

class CountAllStudentsUseCase(
    private val schoolYearRepository: SchoolYearRepository,
    private val sectionRepository: SectionRepository,
    private val studentRepository: StudentRepository,
) {

    suspend operator fun invoke(): Int {
        val schoolYears: List<SchoolYear> = schoolYearRepository.observeAll().first()
        val sections: List<Section> = schoolYears.flatMap { schoolYear ->
            sectionRepository.observeBySchoolYear(schoolYear.id).first()
        }
        return sections.sumOf { section -> studentRepository.listBySection(section.id).size }
    }
}
