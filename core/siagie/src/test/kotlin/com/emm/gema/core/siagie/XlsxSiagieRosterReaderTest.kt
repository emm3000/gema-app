package com.emm.gema.core.siagie

import com.emm.gema.core.domain.siagie.SiagieRoster
import com.emm.gema.core.domain.siagie.SiagieRosterResult
import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Test

class XlsxSiagieRosterReaderTest {

    private val fixtureName: String = "6 Primaria EBR.xlsx"

    private val reader = XlsxSiagieRosterReader()

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
    fun `rejects a file that is not a workbook`() {
        val result: SiagieRosterResult = reader.read("notes.xlsx", "not a workbook".toByteArray())

        assertThat(result).isEqualTo(SiagieRosterResult.NotASiagieTemplate)
    }

    private fun parsed(): SiagieRoster {
        val result: SiagieRosterResult = reader.read(fixtureName, fixture().readBytes())
        return (result as SiagieRosterResult.Parsed).roster
    }

    private fun fixture(): File = File(requireNotNull(javaClass.classLoader.getResource(fixtureName)).toURI())
}
