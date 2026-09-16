package com.emm.gema.home

import android.content.res.Resources
import com.emm.gema.R
import com.emm.gema.core.domain.date.DateNameProvider
import com.emm.gema.core.ui.R as CoreUiR
import java.time.DayOfWeek
import java.time.Month

class AndroidDateNameProvider(private val resources: Resources) : DateNameProvider {

    override fun weekdayName(dayOfWeek: DayOfWeek): String =
        resources.getStringArray(CoreUiR.array.day_names_full)[dayOfWeek.value - 1]

    override fun monthName(month: Month): String =
        resources.getStringArray(CoreUiR.array.month_names_full)[month.value - 1]

    override fun todayPrefix(): String = resources.getString(R.string.home_today_prefix)
}
