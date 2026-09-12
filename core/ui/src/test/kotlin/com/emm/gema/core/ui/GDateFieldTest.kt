package com.emm.gema.core.ui

import com.emm.gema.core.theme.GemaAccessibility
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GDateFieldTest {

    @Test
    fun `isFontScaleExpanded is false below the threshold`() {
        assertThat(isFontScaleExpanded(1.0f)).isFalse()
    }

    @Test
    fun `isFontScaleExpanded is true exactly at the threshold`() {
        assertThat(isFontScaleExpanded(GemaAccessibility.expandableFieldFontScaleThreshold)).isTrue()
    }

    @Test
    fun `isFontScaleExpanded is true above the threshold`() {
        assertThat(isFontScaleExpanded(1.3f)).isTrue()
    }
}
