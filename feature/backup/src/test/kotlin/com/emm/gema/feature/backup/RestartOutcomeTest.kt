package com.emm.gema.feature.backup

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RestartOutcomeTest {

    @Test
    fun `restarts and exits when a launch intent is available`() {
        var launched: Boolean = false
        var exited: Boolean = false

        val outcome: RestartOutcome = performRestart(launch = { launched = true }, exit = { exited = true })

        assertThat(outcome).isEqualTo(RestartOutcome.Restarted)
        assertThat(launched).isTrue()
        assertThat(exited).isTrue()
    }

    @Test
    fun `requires a manual restart when no launch intent is available`() {
        var exited: Boolean = false

        val outcome: RestartOutcome = performRestart(launch = null, exit = { exited = true })

        assertThat(outcome).isEqualTo(RestartOutcome.ManualRestartRequired)
        assertThat(exited).isFalse()
    }
}
