package com.emm.gema.core.domain.siagie

import java.time.Instant

enum class ImportedTemplateKind {
    GRADES,
    ATTENDANCE,
}

class ImportedTemplate(
    val sectionId: String,
    val kind: ImportedTemplateKind,
    val fileName: String,
    val content: ByteArray,
    val importedAt: Instant,
) {
    init {
        require(sectionId.isNotBlank()) { "An imported template belongs to a section" }
        require(fileName.isNotBlank()) { "An imported template keeps the name SIAGIE gave it" }
        require(content.isNotEmpty()) { "An imported template keeps the bytes of the original file" }
    }
}
