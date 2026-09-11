package com.emm.gema.evaluation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelAreaSummary
import com.emm.gema.core.domain.evaluation.PeriodLevelGrid
import com.emm.gema.core.domain.evaluation.PeriodLevelGridRow
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.PeriodLevelSummary
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

private const val PDF_MAGIC_BYTES: String = "%PDF"

@RunWith(AndroidJUnit4::class)
class PdfDocumentPeriodLevelSummaryRendererTest {

    private val renderer: PdfDocumentPeriodLevelSummaryRenderer = PdfDocumentPeriodLevelSummaryRenderer()

    @Test
    fun rendering_a_summary_produces_a_readable_pdf_file() {
        val bytes: ByteArray = renderer.render("3ro A", "II Bimestre", summaryWithOneStudent())

        assertThat(bytes).isNotEmpty()
        assertThat(String(bytes.copyOfRange(0, PDF_MAGIC_BYTES.length), Charsets.US_ASCII)).isEqualTo(PDF_MAGIC_BYTES)
    }

    @Test
    fun rendering_an_empty_summary_still_produces_a_pdf_file() {
        val bytes: ByteArray = renderer.render("3ro A", "II Bimestre", PeriodLevelSummary(emptyList()))

        assertThat(bytes).isNotEmpty()
    }

    @Test
    fun rendering_two_areas_produces_a_pdf_file_without_failing() {
        val ppss = areaOf(Area.PPSS, studentCount = 1)
        val mate = areaOf(Area.MATE, studentCount = 1)

        val bytes: ByteArray = renderer.render("3ro A", "II Bimestre", PeriodLevelSummary(listOf(ppss, mate)))

        assertThat(bytes).isNotEmpty()
    }

    @Test
    fun rendering_many_students_spans_multiple_pages_without_failing() {
        val summary = PeriodLevelSummary(listOf(areaOf(Area.PPSS, studentCount = 80)))

        val bytes: ByteArray = renderer.render("3ro A", "II Bimestre", summary)

        assertThat(bytes).isNotEmpty()
    }

    @Test
    fun rendering_a_c_with_a_conclusion_produces_a_pdf_file_without_failing() {
        val column = competency(Area.PPSS, 1)
        val grid = PeriodLevelGrid(
            columns = listOf(column),
            rows = listOf(
                PeriodLevelGridRow(
                    student = student("student-1", "ACOSTA RIVERA, Luz"),
                    cells = listOf(
                        PeriodLevel(PeriodLevelKey(SectionId("section-1"), PeriodId("period-1"), StudentId("student-1"), column.id))
                            .withAchievementLevel(AchievementLevel.C)
                            .withDescriptiveConclusion("Necesita apoyo"),
                    ),
                ),
            ),
        )

        val bytes: ByteArray = renderer.render(
            "3ro A",
            "II Bimestre",
            PeriodLevelSummary(listOf(PeriodLevelAreaSummary(Area.PPSS, grid))),
        )

        assertThat(bytes).isNotEmpty()
    }

    private fun summaryWithOneStudent(): PeriodLevelSummary =
        PeriodLevelSummary(listOf(areaOf(Area.PPSS, studentCount = 1)))

    private fun areaOf(area: Area, studentCount: Int): PeriodLevelAreaSummary {
        val column = competency(area, 1)
        val rows: List<PeriodLevelGridRow> = (1..studentCount).map { index ->
            PeriodLevelGridRow(
                student = student("student-$index", "ESTUDIANTE $index"),
                cells = listOf(
                    PeriodLevel(PeriodLevelKey(SectionId("section-1"), PeriodId("period-1"), StudentId("student-$index"), column.id))
                        .withAchievementLevel(AchievementLevel.AD),
                ),
            )
        }
        return PeriodLevelAreaSummary(area, PeriodLevelGrid(columns = listOf(column), rows = rows))
    }

    private fun competency(area: Area, siagieOrdinal: Int): Competency =
        Competency(id = Competency.idOf(area, siagieOrdinal), area = area, siagieOrdinal = siagieOrdinal, name = "Comp")

    private fun student(id: String, fullName: String): Student = Student(
        id = StudentId(id),
        sectionId = SectionId("section-1"),
        code = StudentCode(id.filter(Char::isDigit).padStart(14, '0')),
        fullName = fullName,
    )
}
