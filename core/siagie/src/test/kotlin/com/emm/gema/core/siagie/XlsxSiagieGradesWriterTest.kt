package com.emm.gema.core.siagie

import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.siagie.SiagieGradeEntry
import com.emm.gema.core.domain.siagie.SiagieGradesWriteResult
import com.emm.gema.core.domain.student.StudentCode
import com.google.common.truth.Truth.assertThat
import java.io.File
import java.util.zip.ZipFile
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

private const val FIXTURE_NAME: String = "6 Primaria EBR.xlsx"

class XlsxSiagieGradesWriterTest {

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    private val writer = XlsxSiagieGradesWriter()

    @Test
    fun `writes the level and the conclusion of a worked competency`() {
        val filled: File = write(
            entryOf(Area.COMU, 1, "10000000000001", "AD", "Lee con fluidez"),
            entryOf(Area.PPSS, 5, "10000000000003", "Comentario 2", ""),
        )

        val comu: Map<String, String> = XlsxTemplate(filled).readSheet("COMU")
        val ppss: Map<String, String> = XlsxTemplate(filled).readSheet("PPSS")
        assertThat(comu["D4"]).isEqualTo("AD")
        assertThat(comu["E4"]).isEqualTo("Lee con fluidez")
        assertThat(ppss["H6"]).isEqualTo("Comentario 2")
    }

    @Test
    fun `leaves every cell of an untouched competency alone`() {
        val before: Map<String, String> = XlsxTemplate(fixture()).readSheet("COMU")

        val filled: File = write(entryOf(Area.COMU, 1, "10000000000002", "B", ""))

        val after: Map<String, String> = XlsxTemplate(filled).readSheet("COMU")
        assertThat(after.filterKeys { it != "D5" }).containsExactlyEntriesIn(before)
    }

    @Test
    fun `leaves every sheet without entries byte for byte untouched`() {
        val filled: File = write(entryOf(Area.COMU, 1, "10000000000001", "A", ""))

        val original: Map<String, ByteArray> = parts(fixture())
        val written: Map<String, ByteArray> = parts(filled)
        assertThat(written.keys).containsExactlyElementsIn(original.keys).inOrder()
        original.filterKeys { it != "xl/worksheets/sheet1.xml" }.forEach { (name, payload) ->
            assertThat(written[name]).isEqualTo(payload)
        }
    }

    @Test
    fun `reports an area the template has no sheet for instead of dropping it`() {
        val result: SiagieGradesWriteResult = writer.write(
            fixture().readBytes(),
            listOf(
                entryOf(Area.COMU, 1, "10000000000001", "A", ""),
                entryOf(Area.EFIS, 1, "10000000000001", "A", ""),
            ),
        )

        assertThat(result).isEqualTo(
            SiagieGradesWriteResult.Unmapped(areas = listOf(Area.EFIS), studentCodes = emptyList()),
        )
    }

    @Test
    fun `reports a student the template does not carry instead of dropping them`() {
        val result: SiagieGradesWriteResult = writer.write(
            fixture().readBytes(),
            listOf(entryOf(Area.COMU, 1, "99999999999999", "A", "")),
        )

        assertThat(result).isEqualTo(
            SiagieGradesWriteResult.Unmapped(
                areas = emptyList(),
                studentCodes = listOf(StudentCode("99999999999999")),
            ),
        )
    }

    private fun write(vararg entries: SiagieGradeEntry): File {
        val written = writer.write(fixture().readBytes(), entries.toList()) as SiagieGradesWriteResult.Written
        val target: File = File(temporaryFolder.newFolder(), FIXTURE_NAME)
        target.writeBytes(written.content)
        return target
    }

    private fun entryOf(
        area: Area,
        ordinal: Int,
        code: String,
        value: String,
        conclusion: String,
    ): SiagieGradeEntry = SiagieGradeEntry(
        area = area,
        siagieOrdinal = ordinal,
        studentCode = StudentCode(code),
        achievementValue = value,
        descriptiveConclusion = conclusion,
    )

    private fun fixture(): File = File(requireNotNull(javaClass.classLoader.getResource(FIXTURE_NAME)).toURI())

    private fun parts(file: File): Map<String, ByteArray> = ZipFile(file).use { zip ->
        zip.entries().toList().associate { entry ->
            entry.name to zip.getInputStream(entry).use { it.readBytes() }
        }
    }
}
