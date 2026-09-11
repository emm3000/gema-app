package com.emm.gema.core.siagie

import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.domain.siagie.AttendanceExportEntry
import com.emm.gema.core.domain.siagie.AttendanceExportFile
import com.emm.gema.core.domain.student.StudentCode
import com.google.common.truth.Truth.assertThat
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.util.zip.ZipFile
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

private const val FIXTURE_NAME: String = "AsistenciaIE_12345_6_A.xlsx"
private val september: YearMonth = YearMonth.of(2026, 9)

class XlsxMonthlyAttendanceWriterTest {

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    private val documents = FileSiagieDocuments()

    @Test
    fun `fills the day cell for each recorded status and leaves unmarked days blank`() = runTest {
        val exportDirectory: File = temporaryFolder.newFolder("export")
        val writer = XlsxMonthlyAttendanceWriter(documents, exportDirectory)
        val entries = listOf(
            AttendanceExportEntry(
                studentCode = StudentCode("10000000000001"),
                statusesByDate = mapOf(
                    september.atDay(1) to AttendanceStatus.PRESENT,
                    september.atDay(2) to AttendanceStatus.LATE,
                    september.atDay(3) to AttendanceStatus.ABSENT,
                    september.atDay(4) to AttendanceStatus.JUSTIFIED,
                ),
            ),
        )

        val file: AttendanceExportFile = writer.export(fixture().absolutePath, september, entries)

        val cells: Map<String, String> = XlsxTemplate(File(file.path)).readSheet(DATA_SHEET)
        assertThat(cells["D4"]).isEqualTo("P")
        assertThat(cells["E4"]).isEqualTo("T")
        assertThat(cells["F4"]).isEqualTo("F")
        assertThat(cells["G4"]).isEqualTo("FJ")
        assertThat(cells["H4"]).isNull()
    }

    @Test
    fun `matches each student by its code across rows`() = runTest {
        val exportDirectory: File = temporaryFolder.newFolder("export")
        val writer = XlsxMonthlyAttendanceWriter(documents, exportDirectory)
        val entries = listOf(
            AttendanceExportEntry(
                studentCode = StudentCode("10000000000003"),
                statusesByDate = mapOf(september.atDay(1) to AttendanceStatus.ABSENT),
            ),
        )

        val file: AttendanceExportFile = writer.export(fixture().absolutePath, september, entries)

        val cells: Map<String, String> = XlsxTemplate(File(file.path)).readSheet(DATA_SHEET)
        assertThat(cells["D4"]).isNull()
        assertThat(cells["D6"]).isEqualTo("F")
    }

    @Test
    fun `leaves the general sheet byte for byte untouched`() = runTest {
        val exportDirectory: File = temporaryFolder.newFolder("export")
        val writer = XlsxMonthlyAttendanceWriter(documents, exportDirectory)
        val entries = listOf(
            AttendanceExportEntry(
                studentCode = StudentCode("10000000000001"),
                statusesByDate = mapOf(september.atDay(1) to AttendanceStatus.PRESENT),
            ),
        )

        val file: AttendanceExportFile = writer.export(fixture().absolutePath, september, entries)

        val originalGeneralSheet: ByteArray = partBytes(fixture(), "xl/worksheets/sheet1.xml")
        val writtenGeneralSheet: ByteArray = partBytes(File(file.path), "xl/worksheets/sheet1.xml")
        assertThat(writtenGeneralSheet).isEqualTo(originalGeneralSheet)
    }

    @Test
    fun `keeps the file name required by the SIAGIE upload`() = runTest {
        val exportDirectory: File = temporaryFolder.newFolder("export")
        val writer = XlsxMonthlyAttendanceWriter(documents, exportDirectory)

        val file: AttendanceExportFile = writer.export(fixture().absolutePath, september, emptyList())

        assertThat(file.fileName).isEqualTo(FIXTURE_NAME)
        assertThat(File(file.path).name).isEqualTo(FIXTURE_NAME)
    }

    @Test
    fun `a status recorded outside the exported month is ignored`() = runTest {
        val exportDirectory: File = temporaryFolder.newFolder("export")
        val writer = XlsxMonthlyAttendanceWriter(documents, exportDirectory)
        val entries = listOf(
            AttendanceExportEntry(
                studentCode = StudentCode("10000000000001"),
                statusesByDate = mapOf(LocalDate.of(2026, 8, 1) to AttendanceStatus.ABSENT),
            ),
        )

        val file: AttendanceExportFile = writer.export(fixture().absolutePath, september, entries)

        val cells: Map<String, String> = XlsxTemplate(File(file.path)).readSheet(DATA_SHEET)
        assertThat(cells["D4"]).isNull()
    }

    private fun fixture(): File = File(requireNotNull(javaClass.classLoader.getResource(FIXTURE_NAME)).toURI())

    private fun partBytes(file: File, partName: String): ByteArray = ZipFile(file).use { zip ->
        val entry = requireNotNull(zip.getEntry(partName))
        zip.getInputStream(entry).use { it.readBytes() }
    }

    private companion object {
        const val DATA_SHEET: String = "Asistencia"
    }
}
