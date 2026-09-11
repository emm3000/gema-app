package com.emm.gema.core.domain.evaluation

import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.SeedCurriculumUseCase
import com.emm.gema.core.domain.curriculum.SetCompetencyWorkedUseCase
import com.emm.gema.core.domain.fake.InMemoryCompetencyRepository
import com.emm.gema.core.domain.fake.InMemoryPeriodLevelRepository
import com.emm.gema.core.domain.fake.InMemorySectionAreaRepository
import com.emm.gema.core.domain.fake.InMemoryStudentRepository
import com.emm.gema.core.domain.fake.InMemoryWorkedCompetencyRepository
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.nio.charset.StandardCharsets

private const val SECTION_ID: String = "section-1"
private const val PERIOD_ID: String = "period-1"

class ExportPeriodLevelSummaryUseCaseTest {

    private val competencyRepository: InMemoryCompetencyRepository = InMemoryCompetencyRepository()
    private val workedCompetencyRepository: InMemoryWorkedCompetencyRepository = InMemoryWorkedCompetencyRepository()
    private val studentRepository: InMemoryStudentRepository = InMemoryStudentRepository()
    private val sectionAreaRepository: InMemorySectionAreaRepository = InMemorySectionAreaRepository()
    private val periodLevelRepository: InMemoryPeriodLevelRepository = InMemoryPeriodLevelRepository()
    private val documents: FakeSummaryDocuments = FakeSummaryDocuments()
    private val pdfRenderer: FakePeriodLevelSummaryPdfRenderer = FakePeriodLevelSummaryPdfRenderer()

    private val setCompetencyWorked: SetCompetencyWorkedUseCase =
        SetCompetencyWorkedUseCase(workedCompetencyRepository)
    private val export: ExportPeriodLevelSummaryUseCase = ExportPeriodLevelSummaryUseCase(
        getSummary = GetPeriodLevelSummaryUseCase(
            sectionAreaRepository = sectionAreaRepository,
            competencyRepository = competencyRepository,
            workedCompetencyRepository = workedCompetencyRepository,
            studentRepository = studentRepository,
            periodLevelRepository = periodLevelRepository,
        ),
        documents = documents,
        pdfRenderer = pdfRenderer,
    )

    @Test
    fun `exporting as csv writes the rendered table under a csv file name`() = runTest {
        seed()
        addStudent("student-1", "ACOSTA RIVERA, Luz")

        val file: SummaryFile = export(
            sectionId = SECTION_ID,
            periodId = PERIOD_ID,
            sectionTitle = "3ro A",
            periodLabel = "II Bimestre",
            format = SummaryFormat.CSV,
        )

        assertThat(file.name).isEqualTo("resumen-3ro-a-ii-bimestre.csv")
        assertThat(String(documents.written.getValue(file.name), StandardCharsets.UTF_8)).contains("Estudiante")
    }

    @Test
    fun `exporting as pdf delegates rendering to the pdf renderer`() = runTest {
        seed()
        addStudent("student-1", "ACOSTA RIVERA, Luz")

        val file: SummaryFile = export(
            sectionId = SECTION_ID,
            periodId = PERIOD_ID,
            sectionTitle = "3ro A",
            periodLabel = "II Bimestre",
            format = SummaryFormat.PDF,
        )

        assertThat(file.name).isEqualTo("resumen-3ro-a-ii-bimestre.pdf")
        assertThat(documents.written.getValue(file.name)).isEqualTo(FakePeriodLevelSummaryPdfRenderer.RENDERED_BYTES)
    }

    private suspend fun seed() {
        SeedCurriculumUseCase(competencyRepository).invoke()
        setCompetencyWorked(SECTION_ID, PERIOD_ID, Competency.idOf(Area.PPSS, 1), isWorked = true)
    }

    private suspend fun addStudent(id: String, fullName: String) {
        studentRepository.save(
            Student(
                id = id,
                sectionId = SECTION_ID,
                code = StudentCode("1234567890123${id.last()}"),
                fullName = fullName,
            ),
        )
    }
}
