package com.emm.gema.feature.sections.detail

sealed interface SectionDetailUiIntent {

    data object TakeAttendanceClicked : SectionDetailUiIntent

    data object AttendanceClicked : SectionDetailUiIntent

    data object StudentsClicked : SectionDetailUiIntent

    data object PeriodLevelsClicked : SectionDetailUiIntent

    data object ExportClicked : SectionDetailUiIntent

    data object ActivitiesClicked : SectionDetailUiIntent

    data object AreasClicked : SectionDetailUiIntent

    data object RenameClicked : SectionDetailUiIntent

    data object BackClicked : SectionDetailUiIntent
}
