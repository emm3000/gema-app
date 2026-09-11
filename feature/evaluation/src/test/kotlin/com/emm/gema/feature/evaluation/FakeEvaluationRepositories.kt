package com.emm.gema.feature.evaluation

import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.PeriodLevelRepository
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.SectionAreaRepository
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentRepository
import com.emm.gema.core.domain.student.orderedByName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSectionAreaRepository(hidden: Set<Area> = emptySet()) : SectionAreaRepository {

    private val hiddenAreas: MutableStateFlow<Set<Area>> = MutableStateFlow(hidden)

    override fun observeHiddenAreas(sectionId: String): Flow<Set<Area>> = hiddenAreas

    override suspend fun setAreaHidden(sectionId: String, area: Area, isHidden: Boolean) {
        hiddenAreas.value = if (isHidden) hiddenAreas.value + area else hiddenAreas.value - area
    }

    override suspend fun clearSection(sectionId: String) {
        hiddenAreas.value = emptySet()
    }
}

class FakeStudentRepository(students: List<Student> = emptyList()) : StudentRepository {

    private val rows: MutableStateFlow<List<Student>> = MutableStateFlow(students)

    override fun observeBySection(sectionId: String): Flow<List<Student>> = rows
        .map { stored -> stored.filter { it.sectionId == sectionId }.orderedByName() }

    override fun observeCountsBySection(): Flow<Map<String, Int>> = rows
        .map { stored -> stored.filterNot { it.isWithdrawn }.groupingBy { it.sectionId }.eachCount() }

    override suspend fun findById(id: String): Student? = rows.value.find { it.id == id }

    override suspend fun findByCode(sectionId: String, code: StudentCode): Student? = rows.value
        .find { it.sectionId == sectionId && it.code == code }

    override suspend fun save(student: Student) {
        rows.value = rows.value.filterNot { it.id == student.id } + student
    }

    override suspend fun deleteBySection(sectionId: String) {
        rows.value = rows.value.filterNot { it.sectionId == sectionId }
    }
}

class FakePeriodLevelRepository : PeriodLevelRepository {

    private val levels: MutableStateFlow<List<PeriodLevel>> = MutableStateFlow(emptyList())
    var failsOnce: Boolean = false

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
        if (failsOnce) {
            failsOnce = false
            error("write failed")
        }
        levels.value = levels.value.filterNot { it.key == periodLevel.key } + periodLevel
    }

    override suspend fun delete(key: PeriodLevelKey) {
        levels.value = levels.value.filterNot { it.key == key }
    }

    override suspend fun clearSection(sectionId: String) {
        levels.value = levels.value.filterNot { it.key.sectionId == sectionId }
    }
}
