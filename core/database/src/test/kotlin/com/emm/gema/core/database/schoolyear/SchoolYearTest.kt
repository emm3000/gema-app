package com.emm.gema.core.database.schoolyear

import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.UuidIdGenerator
import com.emm.gema.core.database.inMemoryGemaDb
import com.emm.gema.core.domain.schoolyear.CreateSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearsUseCase
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class SchoolYearTest {

    private val database: GemaDb = inMemoryGemaDb()
    private val repository: SchoolYearRepository = SqlDelightSchoolYearRepository(
        database = database,
        dispatcher = UnconfinedTestDispatcher(),
    )
    private val createSchoolYear = CreateSchoolYearUseCase(repository, UuidIdGenerator())
    private val getSchoolYears = GetSchoolYearsUseCase(repository)

    @Test
    fun `a created school year is listed`() = runTest {
        createSchoolYear(
            startDate = LocalDate.of(2026, 3, 2),
            endDate = LocalDate.of(2026, 12, 18),
            periodKind = PeriodKind.BIMESTER,
        )

        val schoolYears: List<SchoolYear> = getSchoolYears().first()

        assertThat(schoolYears).hasSize(1)
        assertThat(schoolYears.single().periodKind).isEqualTo(PeriodKind.BIMESTER)
        assertThat(schoolYears.single().startDate).isEqualTo(LocalDate.of(2026, 3, 2))
    }

    @Test
    fun `school years are listed with the most recent first`() = runTest {
        createSchoolYear(LocalDate.of(2025, 3, 3), LocalDate.of(2025, 12, 19), PeriodKind.TRIMESTER)
        createSchoolYear(LocalDate.of(2026, 3, 2), LocalDate.of(2026, 12, 18), PeriodKind.BIMESTER)

        val schoolYears: List<SchoolYear> = getSchoolYears().first()

        assertThat(schoolYears.map { it.startDate }).containsExactly(
            LocalDate.of(2026, 3, 2),
            LocalDate.of(2025, 3, 3),
        ).inOrder()
    }

    @Test
    fun `a created school year is found by its generated id`() = runTest {
        val created: SchoolYear = createSchoolYear(
            startDate = LocalDate.of(2026, 3, 2),
            endDate = LocalDate.of(2026, 12, 18),
            periodKind = PeriodKind.BIMESTER,
        )

        assertThat(repository.findById(created.id)).isEqualTo(created)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a school year that ends before it starts is rejected`() {
        SchoolYear(
            id = "any",
            startDate = LocalDate.of(2026, 12, 18),
            endDate = LocalDate.of(2026, 3, 2),
            periodKind = PeriodKind.BIMESTER,
        )
    }
}
