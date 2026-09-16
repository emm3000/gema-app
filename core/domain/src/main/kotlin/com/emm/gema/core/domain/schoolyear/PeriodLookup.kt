package com.emm.gema.core.domain.schoolyear

import java.time.LocalDate

fun List<Period>.periodFor(date: LocalDate): Period? = find { it.contains(date) }
