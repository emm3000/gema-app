package com.emm.gema.navigation

import com.emm.gema.core.domain.section.Area
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AreaArgTest {

    @Test
    fun `parses a matching area name`() {
        assertThat(parseArea(Area.MATE.name)).isEqualTo(Area.MATE)
    }

    @Test
    fun `returns null for an unmatched area name`() {
        assertThat(parseArea("NOT_AN_AREA")).isNull()
    }

    @Test
    fun `returns null for a missing area argument`() {
        assertThat(parseArea(null)).isNull()
    }

    @Test
    fun `returns null for a blank area argument`() {
        assertThat(parseArea("")).isNull()
    }
}
