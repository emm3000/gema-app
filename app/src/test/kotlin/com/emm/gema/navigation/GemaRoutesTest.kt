package com.emm.gema.navigation

import com.emm.gema.core.domain.section.SectionId
import com.google.common.truth.Truth.assertThat
import java.net.URLDecoder
import org.junit.Test

private const val DOCUMENT_URI: String =
    "content://com.android.providers.downloads.documents/document/msf%3A1000000042"

class GemaRoutesTest {

    @Test
    fun `the import preview route keeps the document uri in one path segment`() {
        val route: String = GemaRoutes.importPreview(SectionId("section-1"), DOCUMENT_URI)

        val segments: List<String> = route.split("/")
        assertThat(segments).hasSize(3)
        assertThat(segments.first()).isEqualTo("import-preview")
        assertThat(segments[1]).isEqualTo("section-1")
    }

    @Test
    fun `the encoded document uri decodes back to the picked uri`() {
        val route: String = GemaRoutes.importPreview(SectionId("section-1"), DOCUMENT_URI)

        val encoded: String = route.substringAfterLast("/")
        assertThat(URLDecoder.decode(encoded, Charsets.UTF_8.name())).isEqualTo(DOCUMENT_URI)
    }

    @Test
    fun `a file name with spaces and accents survives the round trip`() {
        val uri = "content://documents/6 Primaria EBR año.xlsx"

        val encoded: String = GemaRoutes.importPreview(SectionId("section-1"), uri).substringAfterLast("/")

        assertThat(encoded).doesNotContain(" ")
        assertThat(URLDecoder.decode(encoded, Charsets.UTF_8.name())).isEqualTo(uri)
    }
}
