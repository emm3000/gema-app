package com.emm.gema.core.siagie

import java.io.File
import org.w3c.dom.Document
import org.w3c.dom.Element

class XlsxTemplate(private val source: File) {

    fun sheetNames(): List<String> = open { it.sheetParts().keys.toList() }

    fun readSheet(sheetName: String): Map<String, String> = open { workbook: OpcPackage ->
        val part: String = workbook.sheetPart(sheetName)
        val document: Document = SheetXml.parse(requireNotNull(workbook.readText(part)))
        SheetXml.readCells(document, workbook.sharedStrings())
    }

    fun fill(target: File, edits: Map<String, Map<String, String>>) {
        open { workbook: OpcPackage ->
            val replacements: Map<String, ByteArray> = edits.entries.associate { entry ->
                val part: String = workbook.sheetPart(entry.key)
                val document: Document = SheetXml.parse(requireNotNull(workbook.readText(part)))
                entry.value.forEach { (reference: String, text: String) ->
                    SheetXml.writeCell(document, reference, text)
                }
                part to SheetXml.serialize(document)
            }
            workbook.copyTo(target, replacements)
        }
    }

    private fun <T> open(block: (OpcPackage) -> T): T = OpcPackage(source).use(block)
}

private const val RELATIONSHIP_NAMESPACE: String =
    "http://schemas.openxmlformats.org/officeDocument/2006/relationships"

private const val WORKBOOK_PART: String = "xl/workbook.xml"
private const val WORKBOOK_RELS_PART: String = "xl/_rels/workbook.xml.rels"
private const val SHARED_STRINGS_PART: String = "xl/sharedStrings.xml"

internal fun OpcPackage.sheetParts(): Map<String, String> {
    val workbook: Document = SheetXml.parse(requireNotNull(readText(WORKBOOK_PART)))
    val relationships: Map<String, String> = relationships()
    val sheets: List<Element> = SheetXml.elementsOf(
        workbook.getElementsByTagNameNS(SheetXml.NAMESPACE, "sheet")
    )
    return sheets.associate { sheet: Element ->
        val id: String = sheet.getAttributeNS(RELATIONSHIP_NAMESPACE, "id")
        sheet.getAttribute("name") to requireNotNull(relationships[id])
    }
}

internal fun OpcPackage.sheetPart(sheetName: String): String =
    sheetParts()[sheetName] ?: error("Sheet $sheetName is missing in the template")

internal fun OpcPackage.sharedStrings(): List<String> {
    val xml: String = readText(SHARED_STRINGS_PART) ?: return emptyList()
    val document: Document = SheetXml.parse(xml)
    val items: List<Element> = SheetXml.elementsOf(
        document.getElementsByTagNameNS(SheetXml.NAMESPACE, "si")
    )
    return items.map(SheetXml::joinedText)
}

private fun OpcPackage.relationships(): Map<String, String> {
    val document: Document = SheetXml.parse(requireNotNull(readText(WORKBOOK_RELS_PART)))
    val entries: List<Element> = SheetXml.elementsOf(document.getElementsByTagName("*"))
    return entries
        .filter { it.localName == "Relationship" }
        .associate { it.getAttribute("Id") to normalizePart(it.getAttribute("Target")) }
}

private fun normalizePart(target: String): String =
    if (target.startsWith("/")) target.removePrefix("/") else "xl/$target"
