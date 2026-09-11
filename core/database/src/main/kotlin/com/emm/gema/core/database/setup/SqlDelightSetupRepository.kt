package com.emm.gema.core.database.setup

import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.setup.SetupRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SqlDelightSetupRepository(
    private val database: GemaDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SetupRepository {

    override suspend fun saveAndActivate(
        schoolYear: SchoolYear,
        periods: List<Period>,
        section: Section,
    ): Unit = withContext(dispatcher) {
        database.transaction {
            database.schoolYearQueries.insert(
                id = schoolYear.id,
                start_date = schoolYear.startDate.toString(),
                end_date = schoolYear.endDate.toString(),
                period_kind = schoolYear.periodKind.name,
                label = schoolYear.label,
            )
            periods.forEach { period ->
                database.periodQueries.insert(
                    id = period.id,
                    school_year_id = period.schoolYearId,
                    number = period.number.toLong(),
                    start_date = period.startDate.toString(),
                    end_date = period.endDate.toString(),
                )
            }
            database.sectionQueries.insert(
                id = section.id,
                school_year_id = section.schoolYearId,
                grade = section.grade.name,
                name = section.name,
            )
            database.activeSchoolYearQueries.activate(schoolYear.id)
        }
    }
}
