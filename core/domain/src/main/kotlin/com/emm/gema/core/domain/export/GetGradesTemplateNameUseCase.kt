package com.emm.gema.core.domain.export

import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.emm.gema.core.domain.siagie.SiagieImportStore

class GetGradesTemplateNameUseCase(
    private val store: SiagieImportStore,
) {

    suspend operator fun invoke(sectionId: String): String? =
        store.findTemplate(sectionId, ImportedTemplateKind.GRADES)?.fileName
}
