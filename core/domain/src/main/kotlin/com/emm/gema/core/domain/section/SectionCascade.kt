package com.emm.gema.core.domain.section

interface SectionCascade {
    suspend fun deleteSection(sectionId: SectionId)
}
