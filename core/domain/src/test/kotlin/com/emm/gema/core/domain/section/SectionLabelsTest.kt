package com.emm.gema.core.domain.section

import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SectionLabelsTest {

    @Test
    fun `the grade ordinal label for every primary grade is a Peruvian ordinal word`() {
        assertThat(Grade.entries.map { it.label() })
            .containsExactly("1ro", "2do", "3ro", "4to", "5to", "6to")
            .inOrder()
    }

    @Test
    fun `gradeOrdinalLabel matches Grade label at the first and last grade`() {
        assertThat(gradeOrdinalLabel(1)).isEqualTo(Grade.FIRST.label())
        assertThat(gradeOrdinalLabel(6)).isEqualTo(Grade.SIXTH.label())
    }

    @Test
    fun `sectionTitle combines the grade ordinal and the section name`() {
        assertThat(sectionTitle(Grade.THIRD, "A")).isEqualTo("3ro A")
    }

    @Test
    fun `a section title combines the grade ordinal and the section name`() {
        val section = Section(
            id = SectionId("section"),
            schoolYearId = SchoolYearId("year"),
            grade = Grade.THIRD,
            name = "A",
        )

        assertThat(section.title()).isEqualTo("3ro A")
    }
}
