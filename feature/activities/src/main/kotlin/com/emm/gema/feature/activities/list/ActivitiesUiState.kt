package com.emm.gema.feature.activities.list

import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.schoolyear.PeriodId
import java.time.LocalDate

data class ActivitiesUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val periods: List<PeriodOption> = emptyList(),
    val selectedPeriodId: PeriodId? = null,
    val activities: List<ActivityRow> = emptyList(),
) {
    val periodLabel: String
        get() = periods.find { it.id == selectedPeriodId }?.label.orEmpty()
}

data class PeriodOption(
    val id: PeriodId,
    val label: String,
    val isCurrent: Boolean,
)

data class ActivityRow(
    val id: ActivityId,
    val name: String,
    val date: LocalDate,
    val competencyLabels: List<String>,
    val evidenceRecordedCount: Int,
    val studentCount: Int,
)
