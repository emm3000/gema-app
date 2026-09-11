package com.emm.gema.core.siagie

import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.curriculum.CompetencyRepository
import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.PeriodLevelRepository
import com.emm.gema.core.domain.export.ExportedFile
import com.emm.gema.core.domain.export.SiagieExportStore
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.SectionAreaRepository
import com.emm.gema.core.domain.section.SectionId
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCompetencyRepository(private val competencies: List<Competency>) : CompetencyRepository {

    override suspend fun seed(competencies: List<Competency>, curriculumVersion: Int) = Unit

    override suspend fun findByArea(area: Area): List<Competency> = competencies
        .filter { it.area == area }
        .sortedBy { it.siagieOrdinal }
}

class FakeWorkedCompetencyRepository : WorkedCompetencyRepository {

    private val worked: MutableStateFlow<Set<CompetencyId>> = MutableStateFlow(emptySet())

    override fun observeWorked(sectionId: SectionId, periodId: PeriodId): Flow<Set<CompetencyId>> = worked

    override suspend fun setWorked(
        sectionId: SectionId,
        periodId: PeriodId,
        competencyId: CompetencyId,
        isWorked: Boolean,
    ) {
        worked.value = if (isWorked) worked.value + competencyId else worked.value - competencyId
    }

    override suspend fun clearSection(sectionId: SectionId) {
        worked.value = emptySet()
    }
}

class FakePeriodLevelRepository : PeriodLevelRepository {

    private val levels: MutableStateFlow<List<PeriodLevel>> = MutableStateFlow(emptyList())

    override fun observeByPeriod(sectionId: SectionId, periodId: PeriodId): Flow<List<PeriodLevel>> = levels
        .map { stored -> stored.filter { it.key.sectionId == sectionId && it.key.periodId == periodId } }

    override fun observeRecordedCountsByPeriod(sectionId: SectionId, periodId: PeriodId): Flow<Map<CompetencyId, Int>> =
        MutableStateFlow(emptyMap())

    override fun observeRecordedCountsBySection(sectionId: SectionId): Flow<Map<CompetencyId, Int>> =
        MutableStateFlow(emptyMap())

    override suspend fun find(key: PeriodLevelKey): PeriodLevel? = levels.value.find { it.key == key }

    override suspend fun save(periodLevel: PeriodLevel) {
        levels.value = levels.value.filterNot { it.key == periodLevel.key } + periodLevel
    }

    override suspend fun delete(key: PeriodLevelKey) {
        levels.value = levels.value.filterNot { it.key == key }
    }

    override suspend fun clearSection(sectionId: SectionId) {
        levels.value = levels.value.filterNot { it.key.sectionId == sectionId }
    }
}

class FakeSectionAreaRepository : SectionAreaRepository {

    private val hidden: MutableStateFlow<Set<Area>> = MutableStateFlow(emptySet())

    override fun observeHiddenAreas(sectionId: SectionId): Flow<Set<Area>> = hidden

    override suspend fun setAreaHidden(sectionId: SectionId, area: Area, isHidden: Boolean) {
        hidden.value = if (isHidden) hidden.value + area else hidden.value - area
    }

    override suspend fun clearSection(sectionId: SectionId) {
        hidden.value = emptySet()
    }
}

class FileExportStore(private val directory: File) : SiagieExportStore {

    override suspend fun write(fileName: String, content: ByteArray): ExportedFile {
        val target = File(directory, fileName)
        target.writeBytes(content)
        return ExportedFile(name = target.name, path = target.absolutePath)
    }
}
