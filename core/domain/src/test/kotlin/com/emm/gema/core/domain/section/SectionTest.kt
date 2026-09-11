package com.emm.gema.core.domain.section

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SectionTest {

    @Test(expected = IllegalArgumentException::class)
    fun `a section without a name is rejected`() {
        Section(id = "section", schoolYearId = "year", grade = Grade.FIRST, name = " ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a section without a school year is rejected`() {
        Section(id = "section", schoolYearId = "", grade = Grade.FIRST, name = "A")
    }

    @Test
    fun `the six primary grades are ordered from first to sixth`() {
        assertThat(Grade.entries.map { it.number }).containsExactly(1, 2, 3, 4, 5, 6).inOrder()
    }

    @Test
    fun `primary has the nine curricular areas`() {
        assertThat(Area.entries).hasSize(9)
    }
}
