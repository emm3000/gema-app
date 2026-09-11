package com.emm.gema.core.siagie

import com.emm.gema.core.domain.siagie.SiagieRoster
import com.emm.gema.core.domain.siagie.SiagieRosterResult
import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class XlsxSiagieRosterReaderTest {

    private val fixtureName: String = "6 Primaria EBR.xlsx"

    private val reader = XlsxSiagieRosterReader()

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `reads every student of the template in the order of the file`() {
        val roster: SiagieRoster = parsed()

        assertThat(roster.students.map { it.fullName }).containsExactly(
            "ALVARADO QUISPE, MARIA FERNANDA",
            "BAUTISTA HUAMAN, JOSE LUIS",
            "CHAVEZ MAMANI, ROSA ELENA",
            "DIAZ ROJAS, CARLOS ALBERTO",
            "ESPINOZA VARGAS, LUZ MARINA",
        ).inOrder()
    }

    @Test
    fun `reads the student code and the siagie id of every student`() {
        val roster: SiagieRoster = parsed()

        assertThat(roster.students.first().code.value).isEqualTo("10000000000001")
        assertThat(roster.students.first().siagieId).isEqualTo("1001")
    }

    @Test
    fun `reads the grade the template was generated for`() {
        assertThat(parsed().gradeNumber).isEqualTo(6)
    }

    @Test
    fun `leaves the section unknown when the template does not name it`() {
        assertThat(parsed().sectionName).isNull()
    }

    @Test
    fun `rejects a roster whose student code is missing in the middle`() {
        val broken: File = fixtureWith("B6", "")

        val result: SiagieRosterResult = reader.read(fixtureName, broken.readBytes())

        assertThat(result).isEqualTo(SiagieRosterResult.Malformed(row = 6))
    }

    @Test
    fun `rejects a roster whose student code is not fourteen digits`() {
        val broken: File = fixtureWith("B5", "100000001")

        val result: SiagieRosterResult = reader.read(fixtureName, broken.readBytes())

        assertThat(result).isEqualTo(SiagieRosterResult.Malformed(row = 5))
    }

    @Test
    fun `reads every student when the last rows of the sheet are empty`() {
        val padded: File = fixtureWith("C4", "ALVARADO QUISPE, MARIA F.")

        val result: SiagieRosterResult = reader.read(fixtureName, padded.readBytes())

        assertThat((result as SiagieRosterResult.Parsed).roster.students).hasSize(5)
    }

    @Test
    fun `rejects a file that is not a workbook`() {
        val result: SiagieRosterResult = reader.read("notes.xlsx", "not a workbook".toByteArray())

        assertThat(result).isEqualTo(SiagieRosterResult.NotASiagieTemplate)
    }

    private fun parsed(): SiagieRoster {
        val result: SiagieRosterResult = reader.read(fixtureName, fixture().readBytes())
        return (result as SiagieRosterResult.Parsed).roster
    }

    private fun fixtureWith(reference: String, text: String): File {
        val target = File(temporaryFolder.newFolder(), fixtureName)
        XlsxTemplate(fixture()).fill(target, mapOf("COMU" to mapOf(reference to text)))
        return target
    }

    private fun fixture(): File = File(requireNotNull(javaClass.classLoader.getResource(fixtureName)).toURI())
}
