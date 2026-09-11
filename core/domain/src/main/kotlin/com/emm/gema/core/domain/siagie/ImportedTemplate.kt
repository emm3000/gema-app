package com.emm.gema.core.domain.siagie

import com.emm.gema.core.domain.section.SectionId
import java.time.Instant

enum class ImportedTemplateKind {
    GRADES,
    ATTENDANCE,
}

class ImportedTemplate(
    val sectionId: SectionId,
    val kind: ImportedTemplateKind,
    val fileName: String,
    val content: ByteArray,
    val importedAt: Instant,
) {
    init {
        require(fileName.isNotBlank()) { "An imported template keeps the name SIAGIE gave it" }
        require(content.isNotEmpty()) { "An imported template keeps the bytes of the original file" }
    }
}
