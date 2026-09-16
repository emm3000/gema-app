package com.emm.gema.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DayTickerTest {

    @Test
    fun `emits today immediately`() = runTest {
        val clock: Clock = Clock.fixed(
            LocalDate.of(2026, 9, 10).atStartOfDay(ZoneOffset.UTC).toInstant(),
            ZoneOffset.UTC,
        )

        dayTicker(clock).test {
            assertThat(awaitItem()).isEqualTo(LocalDate.of(2026, 9, 10))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `recomputes the date at the next midnight`() = runTest {
        val clock = MutableClock(
            LocalDate.of(2026, 9, 10).atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant(),
            ZoneOffset.UTC,
        )

        dayTicker(clock).test {
            assertThat(awaitItem()).isEqualTo(LocalDate.of(2026, 9, 10))

            clock.instantValue = LocalDate.of(2026, 9, 11).atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(1)
            advanceTimeBy(2_000)

            assertThat(awaitItem()).isEqualTo(LocalDate.of(2026, 9, 11))
            cancelAndIgnoreRemainingEvents()
        }
    }

    private class MutableClock(initial: Instant, private val zoneId: ZoneId) : Clock() {
        var instantValue: Instant = initial

        override fun instant(): Instant = instantValue
        override fun getZone(): ZoneId = zoneId
        override fun withZone(zone: ZoneId): Clock = error("MutableClock does not support withZone")
    }
}
