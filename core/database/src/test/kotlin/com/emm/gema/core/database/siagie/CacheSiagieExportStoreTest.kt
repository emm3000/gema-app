package com.emm.gema.core.database.siagie

import com.emm.gema.core.domain.export.ExportedFile
import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CacheSiagieExportStoreTest {

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `writes the content under the name siagie gave the template`() = runTest {
        val store = CacheSiagieExportStore(File(temporaryFolder.root, "exports"))

        val exported: ExportedFile = store.write("6 Primaria EBR.xlsx", byteArrayOf(1, 2, 3))

        assertThat(exported.name).isEqualTo("6 Primaria EBR.xlsx")
        assertThat(File(exported.path).readBytes()).isEqualTo(byteArrayOf(1, 2, 3))
    }

    @Test
    fun `a second export replaces the previous file`() = runTest {
        val store = CacheSiagieExportStore(File(temporaryFolder.root, "exports"))
        store.write("6 Primaria EBR.xlsx", byteArrayOf(1))

        val exported: ExportedFile = store.write("6 Primaria EBR.xlsx", byteArrayOf(2))

        assertThat(File(exported.path).readBytes()).isEqualTo(byteArrayOf(2))
        assertThat(File(exported.path).parentFile.listFiles()?.size).isEqualTo(1)
    }
}
