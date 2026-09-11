package com.emm.gema.evaluation

import com.emm.gema.core.domain.evaluation.SummaryFile
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class CacheDirSummaryDocumentsTest {

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `writing creates the summaries directory and the file`() = runTest {
        val directory = File(temporaryFolder.newFolder(), "summaries")
        val documents = CacheDirSummaryDocuments(directory, dispatcher = kotlinx.coroutines.Dispatchers.Unconfined)

        val file: SummaryFile = documents.write("resumen.csv", "a,b\n".toByteArray())

        assertThat(File(directory, "resumen.csv").readText()).isEqualTo("a,b\n")
        assertThat(file.name).isEqualTo("resumen.csv")
        assertThat(file.path).isEqualTo(File(directory, "resumen.csv").absolutePath)
    }

    @Test
    fun `writing twice with the same name replaces the file`() = runTest {
        val directory = File(temporaryFolder.newFolder(), "summaries")
        val documents = CacheDirSummaryDocuments(directory, dispatcher = kotlinx.coroutines.Dispatchers.Unconfined)

        documents.write("resumen.csv", "first".toByteArray())
        documents.write("resumen.csv", "second".toByteArray())

        assertThat(File(directory, "resumen.csv").readText()).isEqualTo("second")
    }
}
