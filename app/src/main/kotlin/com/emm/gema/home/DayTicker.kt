package com.emm.gema.home

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal fun dayTicker(clock: Clock): Flow<LocalDate> = flow {
    while (true) {
        val current: LocalDate = LocalDate.now(clock)
        emit(current)
        val zone: ZoneId = clock.zone
        val nextMidnight: Instant = current.plusDays(1).atStartOfDay(zone).toInstant()
        val millisUntilNextMidnight: Long = nextMidnight.toEpochMilli() - clock.instant().toEpochMilli()
        delay(millisUntilNextMidnight.coerceAtLeast(0))
    }
}
