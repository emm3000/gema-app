package com.emm.gema.feature.activities

import com.emm.gema.core.domain.activity.Activity
import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.activity.ActivityRepository
import com.emm.gema.core.domain.activity.EvidenceLevel
import com.emm.gema.core.domain.activity.EvidenceLevelKey
import com.emm.gema.core.domain.activity.EvidenceLevelRepository
import com.emm.gema.core.domain.activity.EvidenceRecord
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
import com.emm.gema.core.domain.section.SectionAreaRepository
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.section.SectionRepository
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.domain.student.StudentRepository
import com.emm.gema.core.domain.student.orderedByName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSectionRepository(private val sections: List<Section>) : SectionRepository {

    override fun observeBySchoolYear(schoolYearId: SchoolYearId): Flow<List<Section>> =
        MutableStateFlow(sections.filter { it.schoolYearId == schoolYearId })

    override fun observeCountsBySchoolYear(): Flow<Map<SchoolYearId, Int>> =
        MutableStateFlow(sections.groupingBy { it.schoolYearId }.eachCount())

    override suspend fun findById(id: SectionId): Section? = sections.find { it.id == id }

    override suspend fun save(section: Section) = Unit

    override suspend fun delete(id: SectionId) = Unit
}

class FakeSchoolYearRepository(private val schoolYears: List<SchoolYear>) : SchoolYearRepository {

    override fun observeAll(): Flow<List<SchoolYear>> = MutableStateFlow(schoolYears)

    override suspend fun findById(id: SchoolYearId): SchoolYear? = schoolYears.find { it.id == id }

    override suspend fun save(schoolYear: SchoolYear) = Unit
}

class FakePeriodRepository(private val periods: List<Period>) : PeriodRepository {

    override fun observeBySchoolYear(schoolYearId: SchoolYearId): Flow<List<Period>> =
        MutableStateFlow(periods.filter { it.schoolYearId == schoolYearId })

    override suspend fun findBySchoolYear(schoolYearId: SchoolYearId): List<Period> =
        periods.filter { it.schoolYearId == schoolYearId }

    override suspend fun findById(id: PeriodId): Period? = periods.find { it.id == id }

    override suspend fun saveAll(periods: List<Period>) = Unit
}

class FakeStudentRepository(students: List<Student> = emptyList()) : StudentRepository {

    private val rows: MutableStateFlow<List<Student>> = MutableStateFlow(students)

    override fun observeBySection(sectionId: SectionId): Flow<List<Student>> = rows
        .map { stored -> stored.filter { it.sectionId == sectionId }.orderedByName() }

    override fun observeCountsBySection(): Flow<Map<SectionId, Int>> = rows
        .map { stored -> stored.filterNot { it.isWithdrawn }.groupingBy { it.sectionId }.eachCount() }

    override suspend fun listBySection(sectionId: SectionId): List<Student> = rows.value
        .filter { it.sectionId == sectionId }
        .orderedByName()

    override suspend fun findById(id: StudentId): Student? = rows.value.find { it.id == id }

    override suspend fun findByCode(sectionId: SectionId, code: StudentCode): Student? = rows.value
        .find { it.sectionId == sectionId && it.code == code }

    override suspend fun save(student: Student) {
        rows.value = rows.value.filterNot { it.id == student.id } + student
    }

    override suspend fun deleteBySection(sectionId: SectionId) {
        rows.value = rows.value.filterNot { it.sectionId == sectionId }
    }
}

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
        val row: Triple<SectionId, PeriodId, CompetencyId> = Triple(sectionId, periodId, competencyId)
        rows.value = if (isWorked) rows.value + row else rows.value - row
    }

    override suspend fun clearSection(sectionId: SectionId) {
        rows.value = rows.value.filterNotTo(mutableSetOf()) { it.first == sectionId }
    }
}

class FakeSectionAreaRepository(hidden: Set<Area> = emptySet()) : SectionAreaRepository {

    private val hiddenAreas: MutableStateFlow<Set<Area>> = MutableStateFlow(hidden)

    override fun observeHiddenAreas(sectionId: SectionId): Flow<Set<Area>> = hiddenAreas

    override suspend fun setAreaHidden(sectionId: SectionId, area: Area, isHidden: Boolean) {
        hiddenAreas.value = if (isHidden) hiddenAreas.value + area else hiddenAreas.value - area
    }

    override suspend fun clearSection(sectionId: SectionId) {
        hiddenAreas.value = emptySet()
    }
}

class FakeActivityRepository(initial: List<Activity> = emptyList()) : ActivityRepository {

    val activities: MutableStateFlow<List<Activity>> = MutableStateFlow(initial)
    var failsOnce: Boolean = false

    override fun observeByPeriod(sectionId: SectionId, periodId: PeriodId): Flow<List<Activity>> = activities
        .map { stored ->
            stored.filter { it.sectionId == sectionId && it.periodId == periodId }.sortedByDescending { it.date }
        }

    override suspend fun findById(id: ActivityId): Activity? = activities.value.find { it.id == id }

    override suspend fun save(activity: Activity) {
        if (failsOnce) {
            failsOnce = false
            error("write failed")
        }
        activities.value = activities.value.filterNot { it.id == activity.id } + activity
    }

    override suspend fun delete(id: ActivityId) {
        activities.value = activities.value.filterNot { it.id == id }
    }

    override suspend fun clearSection(sectionId: SectionId) {
        activities.value = activities.value.filterNot { it.sectionId == sectionId }
    }
}

class FakeEvidenceLevelRepository(private val activityRepository: FakeActivityRepository) : EvidenceLevelRepository {

    val levels: MutableStateFlow<List<EvidenceLevel>> = MutableStateFlow(emptyList())

    override fun observeByActivity(activityId: ActivityId): Flow<List<EvidenceLevel>> = levels
        .map { stored -> stored.filter { it.key.activityId == activityId } }

    override fun observeRecordedStudentCountsByPeriod(
        sectionId: SectionId,
        periodId: PeriodId,
    ): Flow<Map<ActivityId, Int>> =
        levels.map { stored ->
            val activityIds: Set<ActivityId> = activityRepository.activities.value
                .filter { it.sectionId == sectionId && it.periodId == periodId }
                .map { it.id }
                .toSet()

            stored
                .filter { it.key.activityId in activityIds }
                .groupBy { it.key.activityId }
                .mapValues { (_, recorded) -> recorded.map { it.key.studentId }.distinct().size }
        }

    override fun observeForStudentAndCompetency(
        sectionId: SectionId,
        periodId: PeriodId,
        studentId: StudentId,
        competencyId: CompetencyId,
    ): Flow<List<EvidenceRecord>> = MutableStateFlow(emptyList())

    override suspend fun save(evidenceLevel: EvidenceLevel) {
        levels.value = levels.value.filterNot { it.key == evidenceLevel.key } + evidenceLevel
    }

    override suspend fun delete(key: EvidenceLevelKey) {
        levels.value = levels.value.filterNot { it.key == key }
    }

    override suspend fun deleteByActivity(activityId: ActivityId) {
        levels.value = levels.value.filterNot { it.key.activityId == activityId }
    }

    override suspend fun clearSection(sectionId: SectionId) {
        levels.value = emptyList()
    }
}
