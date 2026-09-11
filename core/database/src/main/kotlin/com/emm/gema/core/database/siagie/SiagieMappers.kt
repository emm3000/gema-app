package com.emm.gema.core.database.siagie

import com.emm.gema.core.database.Imported_template as ImportedTemplateRow
import com.emm.gema.core.domain.siagie.ImportedTemplate
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import java.time.Instant

fun ImportedTemplateRow.toDomain(): ImportedTemplate = ImportedTemplate(
    sectionId = section_id,
    kind = ImportedTemplateKind.valueOf(kind),
    fileName = file_name,
    content = content,
    importedAt = Instant.parse(imported_at),
)
