package com.emm.gema.core.siagie

import java.io.Closeable
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

internal class OpcPackage(source: File) : Closeable {

    private val zip: ZipFile = ZipFile(source)

    fun readBytes(partName: String): ByteArray? {
        val entry: ZipEntry = zip.getEntry(partName) ?: return null
        return zip.getInputStream(entry).use { it.readBytes() }
    }

    fun readText(partName: String): String? = readBytes(partName)?.toString(Charsets.UTF_8)

    fun copyTo(target: File, replacements: Map<String, ByteArray>) {
        ZipOutputStream(target.outputStream().buffered()).use { output: ZipOutputStream ->
            val entries: List<ZipEntry> = zip.entries().toList()
            entries.forEach { entry: ZipEntry ->
                val payload: ByteArray = replacements[entry.name]
                    ?: zip.getInputStream(entry).use { it.readBytes() }
                val copy = ZipEntry(entry.name)
                copy.method = ZipEntry.DEFLATED
                copy.time = entry.time
                output.putNextEntry(copy)
                output.write(payload)
                output.closeEntry()
            }
        }
    }

    override fun close() {
        zip.close()
    }
}
