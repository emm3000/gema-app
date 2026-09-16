package com.emm.gema.home

import com.emm.gema.core.domain.attendance.AttendanceDaySummary
import com.emm.gema.core.domain.section.SectionId
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HasPendingStatusTest {

    private val id: SectionId = SectionId("section-1")

    @Test
    fun `a section with attendance taken and no missing levels is not pending`() {
        val row = SectionRow(id, "3ro A", 30, AttendanceDaySummary(27, 27, 0), missingLevelCount = 0)

        assertThat(hasPendingStatus(row)).isFalse()
    }

    @Test
    fun `a section with attendance untaken is pending`() {
        val row = SectionRow(id, "3ro A", 30, AttendanceDaySummary(0, 0, 0))

        assertThat(hasPendingStatus(row)).isTrue()
    }

    @Test
    fun `a taken section with missing levels is still pending`() {
        val row = SectionRow(id, "3ro A", 30, AttendanceDaySummary(27, 27, 0), missingLevelCount = 5)

        assertThat(hasPendingStatus(row)).isTrue()
    }
}
