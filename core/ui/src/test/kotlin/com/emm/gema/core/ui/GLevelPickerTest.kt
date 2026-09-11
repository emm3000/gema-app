package com.emm.gema.core.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GLevelPickerTest {

    @Test
    fun `re-tapping the selected no evidence chip is a no-op`() {
        var invokedWith: String? = null

        nonDeselectingTap(tapped = null, onSelect = { invokedWith = it })

        assertThat(invokedWith).isNull()
    }

    @Test
    fun `tapping an unselected level chip reports its value`() {
        var invokedWith: String? = null

        nonDeselectingTap(tapped = "B", onSelect = { invokedWith = it })

        assertThat(invokedWith).isEqualTo("B")
    }

    @Test
    fun `tapping the unselected no evidence chip reports its value`() {
        var invokedWith: String? = null

        nonDeselectingTap(tapped = "NO_EVIDENCE", onSelect = { invokedWith = it })

        assertThat(invokedWith).isEqualTo("NO_EVIDENCE")
    }
}
