package com.emm.gema.feature.sections.areas

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RecordedLevelsNoteCountTest {

    @Test
    fun `returns the count when the area is off and has recorded levels`() {
        assertThat(recordedLevelsNoteCount(isActive = false, recordedLevelCount = 12)).isEqualTo(12)
    }

    @Test
    fun `returns null when the area is on`() {
        assertThat(recordedLevelsNoteCount(isActive = true, recordedLevelCount = 12)).isNull()
    }

    @Test
    fun `returns null when the area is off but has no recorded levels`() {
        assertThat(recordedLevelsNoteCount(isActive = false, recordedLevelCount = 0)).isNull()
    }
}
