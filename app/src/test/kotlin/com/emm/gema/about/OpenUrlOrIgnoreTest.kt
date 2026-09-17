package com.emm.gema.about

import android.content.ActivityNotFoundException
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OpenUrlOrIgnoreTest {

    @Test
    fun `does not propagate when nothing can handle the url`() {
        var wasCalled: Boolean = false

        openUrlOrIgnore {
            wasCalled = true
            throw ActivityNotFoundException()
        }

        assertThat(wasCalled).isTrue()
    }

    @Test
    fun `runs the action when something can handle the url`() {
        var wasCalled: Boolean = false

        openUrlOrIgnore { wasCalled = true }

        assertThat(wasCalled).isTrue()
    }
}
