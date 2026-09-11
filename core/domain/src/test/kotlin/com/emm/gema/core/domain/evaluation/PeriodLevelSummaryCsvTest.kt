package com.emm.gema.core.domain.evaluation

import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.nio.charset.StandardCharsets

class PeriodLevelSummaryCsvTest {

    @Test
    fun `the csv starts with a utf-8 bom`() {
        val summary = PeriodLevelSummary(areas = emptyList())

        val bytes: ByteArray = PeriodLevelSummaryCsv.toCsv(summary).toByteArray(StandardCharsets.UTF_8)

        assertThat(bytes.copyOfRange(0, 3)).isEqualTo(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
    }

    @Test
    fun `rows are separated by crlf`() {
        val column: Competency = competency(Area.PPSS, 1)
        val summary = areaSummary(Area.PPSS, column, rows = emptyList())

        val csv: String = PeriodLevelSummaryCsv.toCsv(summary)

        assertThat(csv).contains("\r\n")
        assertThat(csv).doesNotContain("\n\n")
    }

    @Test
    fun `each area starts with a title row and its header row`() {
        val column: Competency = competency(Area.PPSS, 1)
        val summary = areaSummary(Area.PPSS, column, rows = emptyList())

        val lines: List<String> = linesOf(summary)

        assertThat(lines[0]).isEqualTo("Personal Social")
        assertThat(lines[1]).isEqualTo("Estudiante,01 - Competencia 1")
    }

    @Test
    fun `two areas each render their own title, header and rows`() {
        val ppss: Competency = competency(Area.PPSS, 1)
        val mate: Competency = competency(Area.MATE, 1)
        val summary = PeriodLevelSummary(
            areas = listOf(
                PeriodLevelAreaSummary(
                    area = Area.PPSS,
                    grid = PeriodLevelGrid(
                        columns = listOf(ppss),
                        rows = listOf(PeriodLevelGridRow(student("ACOSTA RIVERA, Luz"), listOf(level(ppss.id)))),
                    ),
                ),
                PeriodLevelAreaSummary(
                    area = Area.MATE,
                    grid = PeriodLevelGrid(
                        columns = listOf(mate),
                        rows = listOf(PeriodLevelGridRow(student("ACOSTA RIVERA, Luz"), listOf(level(mate.id)))),
                    ),
                ),
            ),
        )

        val lines: List<String> = linesOf(summary)

        assertThat(lines).containsExactly(
            "Personal Social",
            "Estudiante,01 - Competencia 1",
            "\"ACOSTA RIVERA, Luz\",",
            "",
            "Matemática",
            "Estudiante,01 - Competencia 1",
            "\"ACOSTA RIVERA, Luz\",",
        ).inOrder()
    }

    @Test
    fun `a row holds the student name and an achievement level letter per cell`() {
        val column: Competency = competency(Area.PPSS, 1)
        val summary = areaSummary(
            Area.PPSS,
            column,
            rows = listOf(
                PeriodLevelGridRow(
                    student = student("ACOSTA RIVERA, Luz"),
                    cells = listOf(level(column.id).withAchievementLevel(AchievementLevel.AD)),
                ),
            ),
        )

        assertThat(linesOf(summary)[2]).isEqualTo("\"ACOSTA RIVERA, Luz\",AD")
    }

    @Test
    fun `a C with a descriptive conclusion carries it in the cell`() {
        val column: Competency = competency(Area.PPSS, 1)
        val summary = areaSummary(
            Area.PPSS,
            column,
            rows = listOf(
                PeriodLevelGridRow(
                    student = student("ACOSTA RIVERA, Luz"),
                    cells = listOf(
                        level(column.id).withAchievementLevel(AchievementLevel.C)
                            .withDescriptiveConclusion("Necesita apoyo, con seguimiento"),
                    ),
                ),
            ),
        )

        assertThat(linesOf(summary)[2]).isEqualTo("\"ACOSTA RIVERA, Luz\",\"C: Necesita apoyo, con seguimiento\"")
    }

    @Test
    fun `a C without a conclusion renders as a bare letter`() {
        val column: Competency = competency(Area.PPSS, 1)
        val summary = areaSummary(
            Area.PPSS,
            column,
            rows = listOf(
                PeriodLevelGridRow(
                    student = student("ACOSTA RIVERA, Luz"),
                    cells = listOf(level(column.id).withAchievementLevel(AchievementLevel.C)),
                ),
            ),
        )

        assertThat(linesOf(summary)[2]).isEqualTo("\"ACOSTA RIVERA, Luz\",C")
    }

    @Test
    fun `an unworked comment renders as its label instead of a level`() {
        val column: Competency = competency(Area.PPSS, 1)
        val summary = areaSummary(
            Area.PPSS,
            column,
            rows = listOf(
                PeriodLevelGridRow(
                    student = student("ACOSTA RIVERA, Luz"),
                    cells = listOf(level(column.id).withUnworkedComment(UnworkedComment.OTHER)),
                ),
            ),
        )

        assertThat(linesOf(summary)[2]).isEqualTo("\"ACOSTA RIVERA, Luz\",Otro")
    }

    @Test
    fun `an empty cell renders blank`() {
        val column: Competency = competency(Area.PPSS, 1)
        val summary = areaSummary(
            Area.PPSS,
            column,
            rows = listOf(
                PeriodLevelGridRow(student = student("ACOSTA RIVERA, Luz"), cells = listOf(level(column.id))),
            ),
        )

        assertThat(linesOf(summary)[2]).isEqualTo("\"ACOSTA RIVERA, Luz\",")
    }

    @Test
    fun `a quote inside a value is doubled and the value is quoted`() {
        val column: Competency = competency(Area.PPSS, 1)
        val summary = areaSummary(
            Area.PPSS,
            column,
            rows = listOf(PeriodLevelGridRow(student = student("Jose \"Pepe\""), cells = listOf(level(column.id)))),
        )

        assertThat(linesOf(summary)[2]).isEqualTo("\"Jose \"\"Pepe\"\"\",")
    }

    private fun linesOf(summary: PeriodLevelSummary): List<String> =
        PeriodLevelSummaryCsv.toCsv(summary).removePrefix(PeriodLevelSummaryCsv.BOM).split("\r\n")

    private fun areaSummary(area: Area, column: Competency, rows: List<PeriodLevelGridRow>): PeriodLevelSummary =
        PeriodLevelSummary(
            areas = listOf(PeriodLevelAreaSummary(area = area, grid = PeriodLevelGrid(listOf(column), rows))),
        )

    private fun competency(area: Area, siagieOrdinal: Int): Competency = Competency(
        id = Competency.idOf(area, siagieOrdinal),
        area = area,
        siagieOrdinal = siagieOrdinal,
        name = "Competencia $siagieOrdinal",
    )

    private fun student(fullName: String): Student = Student(
        id = "student-1",
        sectionId = "section-1",
        code = StudentCode("12345678901234"),
        fullName = fullName,
    )

    private fun level(competencyId: String): PeriodLevel = PeriodLevel(
        PeriodLevelKey(
            sectionId = "section-1",
            periodId = "period-1",
            studentId = "student-1",
            competencyId = competencyId,
        ),
    )
}
