package com.emm.gema.feature.evaluation

import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.curriculum.CompetencyRepository
import com.emm.gema.core.domain.curriculum.PrimaryCurriculum
import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.schoolyear.PeriodRepository
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.schoolyear.SchoolYearRepository
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.section.SectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCompetencyRepository : CompetencyRepository {

    private val rows: MutableList<Competency> = PrimaryCurriculum.competencies.toMutableList()

    override suspend fun seed(competencies: List<Competency>, curriculumVersion: Int) {
        rows.clear()
        rows.addAll(competencies)
    }

    override suspend fun findByArea(area: Area): List<Competency> = rows
        .filter { it.area == area }
        .sortedBy { it.siagieOrdinal }
}

class FakeWorkedCompetencyRepository : WorkedCompetencyRepository {

    val rows: MutableStateFlow<Set<Triple<SectionId, PeriodId, CompetencyId>>> = MutableStateFlow(emptySet())
    var failsOnce: Boolean = false

    override fun observeWorked(
        sectionId: SectionId,
        periodId: PeriodId,
    ): Flow<Set<CompetencyId>> = rows.map { current ->
        current.filter { it.first == sectionId && it.second == periodId }.mapTo(mutableSetOf()) { it.third }
    }

    override suspend fun setWorked(
        sectionId: SectionId,
        periodId: PeriodId,
        competencyId: CompetencyId,
        isWorked: Boolean,
    ) {
        if (failsOnce) {
            failsOnce = false
            error("write failed")
        }
        val row: Triple<SectionId, PeriodId, CompetencyId> = Triple(sectionId, periodId, competencyId)
        rows.value = if (isWorked) rows.value + row else rows.value - row
    }

    override suspend fun clearSection(sectionId: SectionId) {
        rows.value = rows.value.filterNotTo(mutableSetOf()) { it.first == sectionId }
    }
}

class FakeSectionRepository(private val sections: List<Section>) : SectionRepository {

    override fun observeBySchoolYear(schoolYearId: SchoolYearId): Flow<List<Section>> =
        MutableStateFlow(sections.filter { it.schoolYearId == schoolYearId })

    override fun observeCountsBySchoolYear(): Flow<Map<SchoolYearId, Int>> =
        MutableStateFlow(sections.groupingBy { it.schoolYearId }.eachCount())

    override suspend fun findById(id: SectionId): Section? = sections.find { it.id == id }

    override suspend fun save(section: Section) = Unit

    override suspend fun delete(id: SectionId) = Unit
}

class FakePeriodRepository(private val periods: List<Period>) : PeriodRepository {

    override fun observeBySchoolYear(schoolYearId: SchoolYearId): Flow<List<Period>> =
        MutableStateFlow(periods.filter { it.schoolYearId == schoolYearId })

    override suspend fun findBySchoolYear(schoolYearId: SchoolYearId): List<Period> =
        periods.filter { it.schoolYearId == schoolYearId }

    override suspend fun findById(id: PeriodId): Period? = periods.find { it.id == id }

    override suspend fun saveAll(periods: List<Period>) = Unit
}

class FakeSchoolYearRepository(private val schoolYears: List<SchoolYear>) : SchoolYearRepository {

    override fun observeAll(): Flow<List<SchoolYear>> = MutableStateFlow(schoolYears)

    override suspend fun findById(id: SchoolYearId): SchoolYear? = schoolYears.find { it.id == id }

    override suspend fun save(schoolYear: SchoolYear) = Unit
}
