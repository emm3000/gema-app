package com.emm.gema.core.domain.evaluation

import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PeriodLevelTest {

    @Test
    fun `a period level starts empty`() {
        val level: PeriodLevel = PeriodLevel(key)

        assertThat(level.isRecorded).isFalse()
        assertThat(level.isEmpty).isTrue()
        assertThat(level.isIncomplete).isFalse()
    }

    @Test
    fun `an achievement level and an unworked comment never coexist`() {
        val failure: IllegalArgumentException = kotlin.runCatching {
            PeriodLevel(key = key, achievementLevel = AchievementLevel.A, unworkedComment = UnworkedComment.OTHER)
        }.exceptionOrNull() as IllegalArgumentException

        assertThat(failure).hasMessageThat().contains("never both")
    }

    @Test
    fun `choosing an achievement level clears the unworked comment`() {
        val level: PeriodLevel = PeriodLevel(key)
            .withUnworkedComment(UnworkedComment.NO_ACTIONS)
            .withAchievementLevel(AchievementLevel.B)

        assertThat(level.achievementLevel).isEqualTo(AchievementLevel.B)
        assertThat(level.unworkedComment).isNull()
    }

    @Test
    fun `choosing an unworked comment clears the achievement level`() {
        val level: PeriodLevel = PeriodLevel(key)
            .withAchievementLevel(AchievementLevel.B)
            .withUnworkedComment(UnworkedComment.NOT_ENOUGH_EVIDENCE)

        assertThat(level.unworkedComment).isEqualTo(UnworkedComment.NOT_ENOUGH_EVIDENCE)
        assertThat(level.achievementLevel).isNull()
    }

    @Test
    fun `a C without a descriptive conclusion is incomplete`() {
        val level: PeriodLevel = PeriodLevel(key).withAchievementLevel(AchievementLevel.C)

        assertThat(level.isRecorded).isTrue()
        assertThat(level.isIncomplete).isTrue()
    }

    @Test
    fun `a C with a descriptive conclusion is complete`() {
        val level: PeriodLevel = PeriodLevel(key)
            .withAchievementLevel(AchievementLevel.C)
            .withDescriptiveConclusion("Necesita acompañamiento en la lectura")

        assertThat(level.isIncomplete).isFalse()
    }

    @Test
    fun `only a C can be incomplete`() {
        val level: PeriodLevel = PeriodLevel(key).withAchievementLevel(AchievementLevel.AD)

        assertThat(level.isIncomplete).isFalse()
    }

    @Test
    fun `clearing the achievement level keeps the descriptive conclusion`() {
        val level: PeriodLevel = PeriodLevel(key)
            .withAchievementLevel(AchievementLevel.C)
            .withDescriptiveConclusion("Texto")
            .withAchievementLevel(null)

        assertThat(level.achievementLevel).isNull()
        assertThat(level.descriptiveConclusion).isEqualTo("Texto")
        assertThat(level.isEmpty).isFalse()
    }

    private val key: PeriodLevelKey = PeriodLevelKey(
        sectionId = SectionId("section-1"),
        periodId = PeriodId("period-1"),
        studentId = firstStudentId,
        competencyId = firstPpssId,
    )
}

private val firstStudentId: StudentId = StudentId("student-1")

private val firstPpssId: CompetencyId = CompetencyId("PPSS-1")
