package com.emm.gema.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import com.emm.gema.core.ui.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth

@Composable
fun DayOfWeek.label(): String {
    val names: Array<String> = stringArrayResource(R.array.day_names_full)
    return names[value - 1]
}

@Composable
fun DayOfWeek.abbreviatedLabel(): String {
    val names: Array<String> = stringArrayResource(R.array.day_names_abbreviated)
    return names[value - 1]
}

@Composable
fun Month.label(): String {
    val names: Array<String> = stringArrayResource(R.array.month_names_full)
    return names[value - 1]
}

@Composable
fun Month.abbreviatedLabel(): String {
    val names: Array<String> = stringArrayResource(R.array.month_names_abbreviated)
    return names[value - 1]
}

@Composable
fun YearMonth.label(): String = "${month.label()} $year"

@Composable
fun LocalDate.label(): String = "${dayOfWeek.abbreviatedLabel()} $dayOfMonth ${month.abbreviatedLabel()} $year"
