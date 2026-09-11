package com.emm.gema.feature.evaluation

import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.activity.EvidenceLevel
import com.emm.gema.core.domain.activity.EvidenceLevelKey
import com.emm.gema.core.domain.activity.EvidenceLevelRepository
import com.emm.gema.core.domain.activity.EvidenceRecord
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.PeriodLevelRepository
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.SectionAreaRepository
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.domain.student.StudentRepository
import com.emm.gema.core.domain.student.orderedByName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

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

class FakePeriodLevelRepository : PeriodLevelRepository {

    private val levels: MutableStateFlow<List<PeriodLevel>> = MutableStateFlow(emptyList())
    var failsOnce: Boolean = false

    override fun observeByPeriod(sectionId: SectionId, periodId: PeriodId): Flow<List<PeriodLevel>> = levels
        .map { stored -> stored.filter { it.key.sectionId == sectionId && it.key.periodId == periodId } }

    override fun observeRecordedCountsByPeriod(
        sectionId: SectionId,
        periodId: PeriodId,
    ): Flow<Map<CompetencyId, Int>> = levels
        .map { stored ->
            stored
                .filter { it.key.sectionId == sectionId && it.key.periodId == periodId && it.isRecorded }
                .groupingBy { it.key.competencyId }
                .eachCount()
        }

    override fun observeRecordedCountsBySection(sectionId: SectionId): Flow<Map<CompetencyId, Int>> = levels
        .map { stored ->
            stored
                .filter { it.key.sectionId == sectionId && it.isRecorded }
                .groupingBy { it.key.competencyId }
                .eachCount()
        }

    override suspend fun find(key: PeriodLevelKey): PeriodLevel? = levels.value.find { it.key == key }

    override suspend fun save(periodLevel: PeriodLevel) {
        if (failsOnce) {
            failsOnce = false
            error("write failed")
        }
        levels.value = levels.value.filterNot { it.key == periodLevel.key } + periodLevel
    }

    override suspend fun delete(key: PeriodLevelKey) {
        levels.value = levels.value.filterNot { it.key == key }
    }

    override suspend fun clearSection(sectionId: SectionId) {
        levels.value = levels.value.filterNot { it.key.sectionId == sectionId }
    }
}

class FakeEvidenceLevelRepository(initial: List<EvidenceRecord> = emptyList()) : EvidenceLevelRepository {

    private val records: MutableStateFlow<List<EvidenceRecord>> = MutableStateFlow(initial)

    override fun observeByActivity(activityId: ActivityId): Flow<List<EvidenceLevel>> = MutableStateFlow(emptyList())

    override fun observeRecordedStudentCountsByPeriod(
        sectionId: SectionId,
        periodId: PeriodId,
    ): Flow<Map<ActivityId, Int>> =
        MutableStateFlow(emptyMap())

    override fun observeForStudentAndCompetency(
        sectionId: SectionId,
        periodId: PeriodId,
        studentId: StudentId,
        competencyId: CompetencyId,
    ): Flow<List<EvidenceRecord>> = records

    override suspend fun save(evidenceLevel: EvidenceLevel) = Unit

    override suspend fun delete(key: EvidenceLevelKey) = Unit

    override suspend fun deleteByActivity(activityId: ActivityId) = Unit

    override suspend fun clearSection(sectionId: SectionId) = Unit
}
