package com.emm.gema.core.domain.backup

sealed interface ReminderThresholdResult {

    data object Saved : ReminderThresholdResult

    data class OutOfRange(
        val minimumDays: Int,
        val maximumDays: Int,
    ) : ReminderThresholdResult
}

class SetReminderThresholdUseCase(
    private val settings: BackupSettingsRepository,
) {

    suspend operator fun invoke(days: Int): ReminderThresholdResult {
        val accepted: IntRange = MINIMUM_REMINDER_THRESHOLD_DAYS..MAXIMUM_REMINDER_THRESHOLD_DAYS
        if (days !in accepted) {
            return ReminderThresholdResult.OutOfRange(
                minimumDays = MINIMUM_REMINDER_THRESHOLD_DAYS,
                maximumDays = MAXIMUM_REMINDER_THRESHOLD_DAYS,
            )
        }
        settings.setReminderThresholdDays(days)
        return ReminderThresholdResult.Saved
    }
}
