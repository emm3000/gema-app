package com.emm.gema.core.siagie

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.util.zip.ZipFile
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class XlsxTemplateTest {

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    private val fixtureName: String = "6 Primaria EBR.xlsx"

    @Test
    fun `lists one sheet per curricular area`() {
        val template = XlsxTemplate(fixture())

        assertThat(template.sheetNames()).containsExactly("COMU", "MATE", "PPSS").inOrder()
    }

    @Test
    fun `reads header and student cells stored as shared strings`() {
        val cells: Map<String, String> = XlsxTemplate(fixture()).readSheet("PPSS")

        assertThat(cells["A3"]).isEqualTo("ID")
        assertThat(cells["B3"]).isEqualTo("CodEstudiante")
        assertThat(cells["D3"]).isEqualTo("Competencia 01 NL")
        assertThat(cells["B4"]).isEqualTo("10000000000001")
        assertThat(cells["C4"]).isEqualTo("ALVARADO QUISPE, MARIA FERNANDA")
        assertThat(cells["A4"]).isEqualTo("1001")
    }

    @Test
    fun `writes achievement level and descriptive conclusion and reopens them`() {
        val target: File = temporaryFolder.newFile(fixtureName)
        val conclusion = "Requiere acompanamiento en la resolucion de problemas de cantidad"

        XlsxTemplate(fixture()).fill(
            target,
            mapOf("PPSS" to mapOf("D4" to "AD", "E4" to conclusion)),
        )

        val cells: Map<String, String> = XlsxTemplate(target).readSheet("PPSS")
        assertThat(cells["D4"]).isEqualTo("AD")
        assertThat(cells["E4"]).isEqualTo(conclusion)
    }

    @Test
    fun `leaves every other cell of the edited sheet untouched`() {
        val target: File = temporaryFolder.newFile(fixtureName)
        val before: Map<String, String> = XlsxTemplate(fixture()).readSheet("PPSS")

        XlsxTemplate(fixture()).fill(target, mapOf("PPSS" to mapOf("D4" to "B")))

        val after: Map<String, String> = XlsxTemplate(target).readSheet("PPSS")
        assertThat(after.filterKeys { it != "D4" }).containsExactlyEntriesIn(before)
    }

    @Test
    fun `keeps dropdown styles and frozen panes of the edited sheet`() {
        val target: File = temporaryFolder.newFile(fixtureName)

        XlsxTemplate(fixture()).fill(target, mapOf("PPSS" to mapOf("D4" to "C")))

        val sheet: String = partText(target, "xl/worksheets/sheet3.xml")
        assertThat(sheet).contains("""sqref="D4:D8 F4:F8 H4:H8"""")
        assertThat(sheet).contains(""""AD,A,B,C,Comentario 1,Comentario 2,Comentario 3"""")
        assertThat(sheet).contains("""state="frozen"""")
        assertThat(sheet).contains("""<c r="D4" s="6" t="inlineStr">""")
        assertThat(sheet).contains("""<c r="E4" s="4" t="n"/>""")
    }

    @Test
    fun `keeps every untouched part byte for byte`() {
        val target: File = temporaryFolder.newFile(fixtureName)

        XlsxTemplate(fixture()).fill(target, mapOf("PPSS" to mapOf("D4" to "A")))

        val original: Map<String, ByteArray> = parts(fixture())
        val written: Map<String, ByteArray> = parts(target)
        assertThat(written.keys).containsExactlyElementsIn(original.keys).inOrder()
        original.filterKeys { it != "xl/worksheets/sheet3.xml" }.forEach { (name, payload) ->
            assertThat(written[name]).isEqualTo(payload)
        }
    }

    @Test
    fun `keeps the file name required by the SIAGIE upload`() {
        val target: File = temporaryFolder.newFile(fixtureName)

        XlsxTemplate(fixture()).fill(target, mapOf("COMU" to mapOf("D4" to "A")))

        assertThat(target.name).isEqualTo(fixture().name)
    }

    @Test
    fun `fills several sheets in a single pass`() {
        val target: File = temporaryFolder.newFile(fixtureName)

        XlsxTemplate(fixture()).fill(
            target,
            mapOf(
                "COMU" to mapOf("D4" to "A"),
                "MATE" to mapOf("F5" to "Comentario 2"),
            ),
        )

        assertThat(XlsxTemplate(target).readSheet("COMU")["D4"]).isEqualTo("A")
        assertThat(XlsxTemplate(target).readSheet("MATE")["F5"]).isEqualTo("Comentario 2")
    }

    @Test
    fun `escapes text that would break the sheet xml`() {
        val target: File = temporaryFolder.newFile(fixtureName)
        val conclusion = "Avanza en <lectura> & escritura"

        XlsxTemplate(fixture()).fill(target, mapOf("COMU" to mapOf("E4" to conclusion)))

        assertThat(XlsxTemplate(target).readSheet("COMU")["E4"]).isEqualTo(conclusion)
    }

    private fun fixture(): File = File(requireNotNull(javaClass.classLoader.getResource(fixtureName)).toURI())

    private fun partText(file: File, partName: String): String =
        requireNotNull(parts(file)[partName]).toString(Charsets.UTF_8)

    private fun parts(file: File): Map<String, ByteArray> = ZipFile(file).use { zip ->
        zip.entries().toList().associate { entry ->
            entry.name to zip.getInputStream(entry).use { it.readBytes() }
        }
    }
}
