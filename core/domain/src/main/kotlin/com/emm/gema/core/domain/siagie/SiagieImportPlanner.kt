package com.emm.gema.core.domain.siagie

import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionRepository
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentRepository

internal sealed interface PlannedImport {

    data class Ready(val plan: SiagieImportPlan, val content: ByteArray) : PlannedImport

    data class Rejected(val reason: SiagieImportRejection) : PlannedImport
}

class SiagieImportPlanner(
    private val sections: SectionRepository,
    private val students: StudentRepository,
    private val documents: SiagieDocuments,
    private val reader: SiagieRosterReader,
) {

    internal suspend fun plan(sectionId: String, uri: String): PlannedImport {
        val section: Section = requireNotNull(sections.findById(sectionId)) {
            "There is no section $sectionId"
        }
        val fileName: String = documents.nameOf(uri)
        val content: ByteArray = documents.readContent(uri)
        val roster: SiagieRoster = when (val result: SiagieRosterResult = reader.read(fileName, content)) {
            is SiagieRosterResult.Parsed -> result.roster
            SiagieRosterResult.NotASiagieTemplate ->
                return PlannedImport.Rejected(SiagieImportRejection.NotASiagieTemplate)
        }
        val rejection: SiagieImportRejection? = rejectionOf(section, roster)
        if (rejection != null) return PlannedImport.Rejected(rejection)
        val plan: SiagieImportPlan = planOf(fileName, roster, students.listBySection(sectionId))
        return PlannedImport.Ready(plan, content)
    }

    private fun rejectionOf(section: Section, roster: SiagieRoster): SiagieImportRejection? {
        if (roster.students.isEmpty()) return SiagieImportRejection.EmptyRoster
        val gradeNumber: Int? = roster.gradeNumber
        if (gradeNumber != null && gradeNumber != section.grade.number) {
            return SiagieImportRejection.GradeMismatch(expected = section.grade.number, found = gradeNumber)
        }
        val sectionName: String? = roster.sectionName
        if (sectionName != null && !sectionName.equals(section.name, ignoreCase = true)) {
            return SiagieImportRejection.SectionMismatch(expected = section.name, found = sectionName)
        }
        return null
    }

    private fun planOf(fileName: String, roster: SiagieRoster, enrolled: List<Student>): SiagieImportPlan {
        val entries: List<SiagieRosterStudent> = roster.students.distinctBy { it.code }
        val byCode: Map<StudentCode, Student> = enrolled.associateBy { it.code }
        val rosterCodes: Set<StudentCode> = entries.map { it.code }.toSet()
        return SiagieImportPlan(
            fileName = fileName,
            rosterSize = entries.size,
            created = entries.filterNot { it.code in byCode }.map { it.asEntry(studentId = null) },
            updated = entries.mapNotNull { entry -> entry.asUpdate(byCode[entry.code]) },
            missing = enrolled
                .filterNot { it.isWithdrawn || it.code in rosterCodes }
                .map { SiagieImportMissing(studentId = it.id, fullName = it.fullName) },
        )
    }

    private fun SiagieRosterStudent.asUpdate(enrolled: Student?): SiagieImportEntry? {
        if (enrolled == null || !enrolled.isChangedBy(this)) return null
        return asEntry(studentId = enrolled.id)
    }

    private fun SiagieRosterStudent.asEntry(studentId: String?): SiagieImportEntry = SiagieImportEntry(
        studentId = studentId,
        code = code,
        fullName = fullName,
        siagieId = siagieId,
    )

    private fun Student.isChangedBy(entry: SiagieRosterStudent): Boolean = fullName != entry.fullName ||
        isWithdrawn ||
        (entry.siagieId != null && entry.siagieId != siagieId)
}
