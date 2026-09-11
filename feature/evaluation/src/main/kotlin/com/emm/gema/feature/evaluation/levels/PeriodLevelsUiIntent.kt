package com.emm.gema.feature.evaluation.levels

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.evaluation.UnworkedComment
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.Area

sealed interface PeriodLevelsUiIntent {

    data class AreaSelected(val area: Area) : PeriodLevelsUiIntent

    data class PeriodSelected(val periodId: PeriodId) : PeriodLevelsUiIntent

    data class CellClicked(val key: PeriodLevelCellKey) : PeriodLevelsUiIntent

    data object MissingFilterToggled : PeriodLevelsUiIntent

    data class EnterColumnMode(val competencyId: CompetencyId) : PeriodLevelsUiIntent

    data class PickLevelForCurrent(val level: AchievementLevel?) : PeriodLevelsUiIntent

    data object ExitColumnMode : PeriodLevelsUiIntent

    data object WorkedCompetenciesClicked : PeriodLevelsUiIntent

    data class SheetAchievementLevelSelected(val level: AchievementLevel?) : PeriodLevelsUiIntent

    data class SheetUnworkedCommentSelected(val comment: UnworkedComment?) : PeriodLevelsUiIntent

    data class SheetDescriptiveConclusionChanged(val value: String) : PeriodLevelsUiIntent

    data object SheetDismissed : PeriodLevelsUiIntent

    data object BackClicked : PeriodLevelsUiIntent
}
