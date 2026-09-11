package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.schoolyear.PeriodRepository
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryPeriodRepository : PeriodRepository {

    private val periods: MutableStateFlow<List<Period>> = MutableStateFlow(emptyList())

    override fun observeBySchoolYear(schoolYearId: SchoolYearId): Flow<List<Period>> = periods
        .map { stored -> stored.filter { it.schoolYearId == schoolYearId }.sortedBy { it.number } }

    override suspend fun findBySchoolYear(schoolYearId: SchoolYearId): List<Period> = periods.value
        .filter { it.schoolYearId == schoolYearId }
        .sortedBy { it.number }

    override suspend fun findById(id: PeriodId): Period? = periods.value.find { it.id == id }

    override suspend fun saveAll(periods: List<Period>) {
        val incoming: Set<PeriodId> = periods.map { it.id }.toSet()
        this.periods.value = this.periods.value.filterNot { it.id in incoming } + periods
    }
}
