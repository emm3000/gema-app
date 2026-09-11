package com.emm.gema.core.ui.share

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ShareFileTest {

    @Test
    fun `builds a send intent spec with the given mime type stream and title`() {
        val spec: ShareIntentSpec = buildShareIntentSpec(
            streamUri = "content://com.emm.gema.shared_files/exports/report.pdf",
            mimeType = "application/pdf",
            chooserTitle = "Compartir reporte",
        )

        assertThat(spec.action).isEqualTo(Intent.ACTION_SEND)
        assertThat(spec.mimeType).isEqualTo("application/pdf")
        assertThat(spec.streamUri).isEqualTo("content://com.emm.gema.shared_files/exports/report.pdf")
        assertThat(spec.chooserTitle).isEqualTo("Compartir reporte")
    }

    @Test
    fun `grants read permission on the shared uri`() {
        val spec: ShareIntentSpec = buildShareIntentSpec(
            streamUri = "content://com.emm.gema.shared_files/backups/gema.gema",
            mimeType = "application/octet-stream",
            chooserTitle = "Compartir copia de seguridad",
        )

        assertThat(spec.flags).isEqualTo(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}
