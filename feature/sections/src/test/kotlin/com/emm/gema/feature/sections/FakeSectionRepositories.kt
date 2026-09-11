package com.emm.gema.feature.sections

import com.emm.gema.core.domain.activity.Activity
import com.emm.gema.core.domain.activity.ActivityRepository
import com.emm.gema.core.domain.activity.EvidenceLevel
import com.emm.gema.core.domain.activity.EvidenceLevelKey
import com.emm.gema.core.domain.activity.EvidenceLevelRepository
import com.emm.gema.core.domain.attendance.AttendanceRecord
import com.emm.gema.core.domain.attendance.AttendanceRepository
import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.PeriodLevelRepository
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodRepository
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearRepository
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionAreaRepository
import com.emm.gema.core.domain.section.SectionRepository
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentRepository
import com.emm.gema.core.domain.student.orderedByName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import com.emm.gema.core.domain.siagie.ImportedTemplate
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.emm.gema.core.domain.siagie.SiagieImportStore
import java.time.LocalDate
import java.time.YearMonth

class FakeSectionRepository(initial: List<Section> = emptyList()) : SectionRepository {

    val sections: MutableStateFlow<List<Section>> = MutableStateFlow(initial)
    var failsOnce: Boolean = false

    override fun observeBySchoolYear(schoolYearId: String): Flow<List<Section>> = sections
        .map { stored -> stored.filter { it.schoolYearId == schoolYearId } }

    override fun observeCountsBySchoolYear(): Flow<Map<String, Int>> = sections
        .map { stored -> stored.groupingBy { it.schoolYearId }.eachCount() }

    override suspend fun findById(id: String): Section? = sections.value.find { it.id == id }

    override suspend fun save(section: Section) {
        failOnce()
        sections.value = sections.value.filterNot { it.id == section.id } + section
    }

    override suspend fun delete(id: String) {
        failOnce()
        sections.value = sections.value.filterNot { it.id == id }
    }

    private fun failOnce() {
        if (!failsOnce) return
        failsOnce = false
        error("the disk is full")
    }
}

class FakeSectionAreaRepository : SectionAreaRepository {

    val hiddenAreas: MutableStateFlow<Map<String, Set<Area>>> = MutableStateFlow(emptyMap())

    override fun observeHiddenAreas(sectionId: String): Flow<Set<Area>> = hiddenAreas
        .map { stored -> stored[sectionId].orEmpty() }

    override suspend fun setAreaHidden(sectionId: String, area: Area, isHidden: Boolean) {
        val current: Set<Area> = hiddenAreas.value[sectionId].orEmpty()
        hiddenAreas.value = hiddenAreas.value + (sectionId to if (isHidden) current + area else current - area)
    }

    override suspend fun clearSection(sectionId: String) {
        hiddenAreas.value = hiddenAreas.value - sectionId
    }
}

class FakeWorkedCompetencyRepository : WorkedCompetencyRepository {

    val rows: MutableStateFlow<Set<Triple<String, String, String>>> = MutableStateFlow(emptySet())

    override fun observeWorked(sectionId: String, periodId: String): Flow<Set<String>> = rows.map { current ->
        current.filter { it.first == sectionId && it.second == periodId }.mapTo(mutableSetOf()) { it.third }
    }

    override suspend fun setWorked(
        sectionId: String,
        periodId: String,
        competencyId: String,
        isWorked: Boolean,
    ) {
        val row: Triple<String, String, String> = Triple(sectionId, periodId, competencyId)
        rows.value = if (isWorked) rows.value + row else rows.value - row
    }

    override suspend fun clearSection(sectionId: String) {
        rows.value = rows.value.filterNotTo(mutableSetOf()) { it.first == sectionId }
    }
}

class FakeStudentRepository(initial: List<Student> = emptyList()) : StudentRepository {

    val students: MutableStateFlow<List<Student>> = MutableStateFlow(initial)

    override fun observeBySection(sectionId: String): Flow<List<Student>> = students
        .map { stored -> stored.filter { it.sectionId == sectionId }.orderedByName() }

    override fun observeCountsBySection(): Flow<Map<String, Int>> = students
        .map { stored -> stored.filterNot { it.isWithdrawn }.groupingBy { it.sectionId }.eachCount() }

    override suspend fun listBySection(sectionId: String): List<Student> = students.value
        .filter { it.sectionId == sectionId }
        .orderedByName()

    override suspend fun findById(id: String): Student? = students.value.find { it.id == id }

    override suspend fun findByCode(sectionId: String, code: StudentCode): Student? = students.value
        .find { it.sectionId == sectionId && it.code == code }

    override suspend fun save(student: Student) {
        students.value = students.value.filterNot { it.id == student.id } + student
    }

    override suspend fun deleteBySection(sectionId: String) {
        students.value = students.value.filterNot { it.sectionId == sectionId }
    }
}

class FakeSiagieImportStore : SiagieImportStore {

    private val templates: MutableMap<String, ImportedTemplate> = mutableMapOf()

    override suspend fun apply(students: List<Student>, template: ImportedTemplate) {
        templates[template.sectionId] = template
    }

    override suspend fun clearSection(sectionId: String) {
        templates.remove(sectionId)
    }

    override suspend fun findTemplate(sectionId: String, kind: ImportedTemplateKind): ImportedTemplate? =
        templates[sectionId]?.takeIf { it.kind == kind }
}

class FakePeriodLevelRepository : PeriodLevelRepository {

    private val levels: MutableStateFlow<List<PeriodLevel>> = MutableStateFlow(emptyList())

    override fun observeByPeriod(sectionId: String, periodId: String): Flow<List<PeriodLevel>> = levels
        .map { stored -> stored.filter { it.key.sectionId == sectionId && it.key.periodId == periodId } }

    override fun observeRecordedCountsByPeriod(sectionId: String, periodId: String): Flow<Map<String, Int>> = levels
        .map { stored ->
            stored
                .filter { it.key.sectionId == sectionId && it.key.periodId == periodId && it.isRecorded }
                .groupingBy { it.key.competencyId }
                .eachCount()
        }

    override fun observeRecordedCountsBySection(sectionId: String): Flow<Map<String, Int>> = levels
        .map { stored ->
            stored
                .filter { it.key.sectionId == sectionId && it.isRecorded }
                .groupingBy { it.key.competencyId }
                .eachCount()
        }

    override suspend fun find(key: PeriodLevelKey): PeriodLevel? = levels.value.find { it.key == key }

    override suspend fun save(periodLevel: PeriodLevel) {
        levels.value = levels.value.filterNot { it.key == periodLevel.key } + periodLevel
    }

    override suspend fun delete(key: PeriodLevelKey) {
        levels.value = levels.value.filterNot { it.key == key }
    }

    override suspend fun clearSection(sectionId: String) {
        levels.value = levels.value.filterNot { it.key.sectionId == sectionId }
    }
}

class FakePeriodRepository(private val periods: List<Period> = emptyList()) : PeriodRepository {

    override fun observeBySchoolYear(schoolYearId: String): Flow<List<Period>> =
        MutableStateFlow(periods.filter { it.schoolYearId == schoolYearId })

    override suspend fun findBySchoolYear(schoolYearId: String): List<Period> =
        periods.filter { it.schoolYearId == schoolYearId }

    override suspend fun findById(id: String): Period? = periods.find { it.id == id }

    override suspend fun saveAll(periods: List<Period>) = Unit
}

class FakeSchoolYearRepository(private val schoolYears: List<SchoolYear> = emptyList()) : SchoolYearRepository {

    override fun observeAll(): Flow<List<SchoolYear>> = MutableStateFlow(schoolYears)

    override suspend fun findById(id: String): SchoolYear? = schoolYears.find { it.id == id }

    override suspend fun save(schoolYear: SchoolYear) = Unit
}
class FakeAttendanceRepository(initial: List<AttendanceRecord> = emptyList()) : AttendanceRepository {

    val records: MutableStateFlow<List<AttendanceRecord>> = MutableStateFlow(initial)

    override fun observeBySectionAndDate(sectionId: String, date: LocalDate): Flow<List<AttendanceRecord>> =
        records.map { stored -> stored.filter { it.sectionId == sectionId && it.date == date } }

    override fun observeBySectionAndMonth(sectionId: String, month: YearMonth): Flow<List<AttendanceRecord>> =
        records.map { stored ->
            stored.filter { it.sectionId == sectionId && YearMonth.from(it.date) == month }
        }

    override suspend fun record(record: AttendanceRecord) {
        records.value = records.value
            .filterNot { it.studentId == record.studentId && it.date == record.date } + record
    }

    override suspend fun countRecordedDays(sectionId: String): Int = records.value
        .filter { it.sectionId == sectionId }
        .distinctBy { it.date }
        .size

    override suspend fun deleteBySection(sectionId: String) {
        records.value = records.value.filterNot { it.sectionId == sectionId }
    }
}

class FakeActivityRepository : ActivityRepository {

    val activities: MutableStateFlow<List<Activity>> = MutableStateFlow(emptyList())

    override fun observeByPeriod(sectionId: String, periodId: String): Flow<List<Activity>> = activities
        .map { stored -> stored.filter { it.sectionId == sectionId && it.periodId == periodId } }

    override suspend fun findById(id: String): Activity? = activities.value.find { it.id == id }

    override suspend fun save(activity: Activity) {
        activities.value = activities.value.filterNot { it.id == activity.id } + activity
    }

    override suspend fun delete(id: String) {
        activities.value = activities.value.filterNot { it.id == id }
    }

    override suspend fun clearSection(sectionId: String) {
        activities.value = activities.value.filterNot { it.sectionId == sectionId }
    }
}

class FakeEvidenceLevelRepository : EvidenceLevelRepository {

    val levels: MutableStateFlow<List<EvidenceLevel>> = MutableStateFlow(emptyList())

    override fun observeByActivity(activityId: String): Flow<List<EvidenceLevel>> = levels
        .map { stored -> stored.filter { it.key.activityId == activityId } }

    override fun observeRecordedStudentCountsByPeriod(sectionId: String, periodId: String): Flow<Map<String, Int>> =
        MutableStateFlow(emptyMap())

    override suspend fun save(evidenceLevel: EvidenceLevel) {
        levels.value = levels.value.filterNot { it.key == evidenceLevel.key } + evidenceLevel
    }

    override suspend fun delete(key: EvidenceLevelKey) {
        levels.value = levels.value.filterNot { it.key == key }
    }

    override suspend fun deleteByActivity(activityId: String) {
        levels.value = levels.value.filterNot { it.key.activityId == activityId }
    }

    override suspend fun clearSection(sectionId: String) {
        levels.value = emptyList()
    }
}
