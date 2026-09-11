package com.emm.gema.core.siagie

import java.io.ByteArrayOutputStream
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

internal object SheetXml {

    const val NAMESPACE: String = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"

    private const val TYPE_SHARED: String = "s"
    private const val TYPE_INLINE: String = "inlineStr"
    private const val TYPE_FORMULA_STRING: String = "str"

    fun parse(xml: String): Document {
        val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val builder: DocumentBuilder = factory.newDocumentBuilder()
        return builder.parse(InputSource(xml.reader()))
    }

    fun serialize(document: Document): ByteArray {
        val transformer: Transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        transformer.setOutputProperty(OutputKeys.INDENT, "no")
        val buffer = ByteArrayOutputStream()
        transformer.transform(DOMSource(document), StreamResult(buffer))
        return buffer.toByteArray()
    }

    fun readCells(document: Document, sharedStrings: List<String>): Map<String, String> {
        val cells: NodeList = document.getElementsByTagNameNS(NAMESPACE, "c")
        val values: MutableMap<String, String> = LinkedHashMap()
        elementsOf(cells).forEach { cell: Element ->
            val reference: String = cell.getAttribute("r")
            val text: String? = cellText(cell, sharedStrings)
            if (reference.isNotEmpty() && text != null) {
                values[reference] = text
            }
        }
        return values
    }

    fun writeCell(document: Document, reference: String, text: String) {
        val cell: Element = cellFor(document, reference)
        while (cell.hasChildNodes()) {
            cell.removeChild(cell.firstChild)
        }
        cell.setAttribute("t", TYPE_INLINE)
        val inline: Element = document.createElementNS(NAMESPACE, "is")
        val value: Element = document.createElementNS(NAMESPACE, "t")
        value.setAttribute("xml:space", "preserve")
        value.appendChild(document.createTextNode(text))
        inline.appendChild(value)
        cell.appendChild(inline)
    }

    private fun cellText(cell: Element, sharedStrings: List<String>): String? {
        return when (cell.getAttribute("t")) {
            TYPE_SHARED -> childText(cell, "v")?.toIntOrNull()?.let(sharedStrings::getOrNull)
            TYPE_INLINE -> firstChild(cell, "is")?.let(::joinedText)
            TYPE_FORMULA_STRING -> childText(cell, "v")
            else -> childText(cell, "v")
        }
    }

    private fun cellFor(document: Document, reference: String): Element {
        val rowNumber: String = reference.dropWhile { it.isLetter() }
        val row: Element = elementsOf(document.getElementsByTagNameNS(NAMESPACE, "row"))
            .firstOrNull { it.getAttribute("r") == rowNumber }
            ?: error("Row $rowNumber is missing in the template")
        val existing: Element? = elementsOf(row.getElementsByTagNameNS(NAMESPACE, "c"))
            .firstOrNull { it.getAttribute("r") == reference }
        if (existing != null) return existing
        val created: Element = document.createElementNS(NAMESPACE, "c")
        created.setAttribute("r", reference)
        val successor: Element? = elementsOf(row.getElementsByTagNameNS(NAMESPACE, "c"))
            .firstOrNull { compareColumns(columnOf(it.getAttribute("r")), columnOf(reference)) > 0 }
        row.insertBefore(created, successor)
        return created
    }

    private fun columnOf(reference: String): String = reference.takeWhile { it.isLetter() }

    private fun compareColumns(left: String, right: String): Int {
        val byLength: Int = left.length.compareTo(right.length)
        return if (byLength != 0) byLength else left.compareTo(right)
    }

    fun joinedText(node: Element): String {
        val texts: List<Element> = elementsOf(node.getElementsByTagNameNS(NAMESPACE, "t"))
        return texts.joinToString(separator = "") { it.textContent }
    }

    private fun firstChild(parent: Element, name: String): Element? =
        elementsOf(parent.getElementsByTagNameNS(NAMESPACE, name)).firstOrNull()

    private fun childText(parent: Element, name: String): String? =
        firstChild(parent, name)?.textContent

    fun elementsOf(nodes: NodeList): List<Element> {
        val elements: MutableList<Element> = ArrayList(nodes.length)
        for (index: Int in 0 until nodes.length) {
            val node: Node = nodes.item(index)
            if (node is Element) elements.add(node)
        }
        return elements
    }
}
