package com.emm.gema.core.domain.curriculum

import com.emm.gema.core.domain.section.Area
import com.google.common.truth.Truth.assertThat
import org.junit.Test

private const val PRIMARY_COMPETENCY_COUNT: Int = 28

class PrimaryCurriculumTest {

    @Test
    fun `the seed carries every primary competency of the CNEB`() {
        assertThat(PrimaryCurriculum.competencies).hasSize(PRIMARY_COMPETENCY_COUNT)
    }

    @Test
    fun `every area of the curriculum has at least one competency`() {
        val covered: Set<Area> = PrimaryCurriculum.competencies.mapTo(mutableSetOf()) { it.area }

        assertThat(covered).containsExactlyElementsIn(Area.entries)
    }

    @Test
    fun `siagie ordinals run from one without gaps inside each area`() {
        Area.entries.forEach { area ->
            val ordinals: List<Int> = PrimaryCurriculum.of(area).map { it.siagieOrdinal }

            assertThat(ordinals).isEqualTo(List(ordinals.size) { it + 1 })
        }
    }

    @Test
    fun `every competency id is unique`() {
        val ids: List<String> = PrimaryCurriculum.competencies.map { it.id }

        assertThat(ids).containsNoDuplicates()
    }

    @Test
    fun `a competency id names its area and its siagie ordinal`() {
        assertThat(Competency.idOf(Area.PPSS, 5)).isEqualTo("PPSS-5")
    }

    @Test
    fun `personal social keeps the official competency order`() {
        val names: List<String> = PrimaryCurriculum.of(Area.PPSS).map { it.name }

        assertThat(names).containsExactly(
            "Construye su identidad",
            "Convive y participa democráticamente",
            "Construye interpretaciones históricas",
            "Gestiona responsablemente el espacio y el ambiente",
            "Gestiona responsablemente los recursos económicos",
        ).inOrder()
    }

    @Test
    fun `every area carries its official name`() {
        assertThat(Area.MATE.officialName).isEqualTo("Matemática")
        assertThat(Area.CIENC_TEC.officialName).isEqualTo("Ciencia y Tecnología")
    }
}
