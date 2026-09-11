package com.emm.gema.core.domain.evaluation

data class PeriodLevel(
    val key: PeriodLevelKey,
    val achievementLevel: AchievementLevel? = null,
    val unworkedComment: UnworkedComment? = null,
    val descriptiveConclusion: String = "",
) {
    init {
        require(achievementLevel == null || unworkedComment == null) {
            "A period level holds an achievement level or an unworked comment, never both"
        }
    }

    val isRecorded: Boolean get() = achievementLevel != null || unworkedComment != null

    val isIncomplete: Boolean get() = achievementLevel == AchievementLevel.C && descriptiveConclusion.isBlank()

    val isEmpty: Boolean get() = !isRecorded && descriptiveConclusion.isBlank()

    fun withAchievementLevel(level: AchievementLevel?): PeriodLevel =
        copy(achievementLevel = level, unworkedComment = null)

    fun withUnworkedComment(comment: UnworkedComment?): PeriodLevel =
        copy(unworkedComment = comment, achievementLevel = null)

    fun withDescriptiveConclusion(text: String): PeriodLevel = copy(descriptiveConclusion = text)
}
