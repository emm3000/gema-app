---
status: accepted
---
# SIAGIE workbook IO uses targeted OPC edits instead of a spreadsheet library

ADR 0002 commits us to filling the template SIAGIE generated instead of building
a workbook. That turns the problem from "write a spreadsheet" into "change a few
cells of an existing package and touch nothing else": sheet order, hidden ids,
styles, the AD/A/B/C dropdowns and the file name all have to survive, because
SIAGIE rejects the upload otherwise.

An `.xlsx` file is an OPC package, a zip of XML parts. A cell lives in
`xl/worksheets/sheetN.xml`. Writing one cell therefore needs exactly two
capabilities: copy a zip entry by entry, and edit one XML part. Both ship with
the platform on Android API 26 and on the JVM.

We write our own reader and writer over `java.util.zip` and the DOM parser in
`javax.xml.parsers` / `javax.xml.transform`. No spreadsheet library enters the
dependency graph.

## Considered Options

- **Apache POI (`poi-ooxml` 5.4.1)**: the only library that models the whole
  format, and the only one that could preserve the template if it were usable.
  It resolves 13 jars totalling **18.9 MiB** (`poi-ooxml-lite` 5.8 MiB,
  `poi` 2.9 MiB, `xmlbeans` 2.2 MiB, `commons-math3` 2.2 MiB), references
  `java.awt.*` and `javax.xml.stream.*` — neither exists on Android — and drives
  XMLBeans through reflection, so R8 needs keep rules discovered by crashing.
  Reaching Android at all means a repackaged fork that trails upstream.
  On top of that, `XSSFWorkbook` rewrites every part of the package on save,
  so "touched nothing else" becomes something we would have to prove against a
  library we do not control. Rejected.
- **`fastexcel` + `fastexcel-reader` 0.19.0**: 9 jars, **3.3 MiB**, but the
  writer only streams a new workbook and the reader is read-only, so filling an
  existing template is outside its design. Its `aalto-xml` parser also needs the
  StAX API that Android lacks. Rejected on capability, not on size.
- **Generating the workbook from the observed structure**: already rejected in
  ADR 0002.

## Consequences

- The capability costs **0 bytes of dependencies**; the compiled module is an
  18 KiB jar with 4 classes, so APK size, method count and R8 rules are all
  unaffected.
- Every API used is available from Android API 8 or earlier
  (`java.util.zip.ZipFile` 1, `DocumentBuilderFactory` 1, `TransformerFactory` 8,
  `DOMSource` 8, `StreamResult` 8, per `data/api-versions.xml`), so API 26 needs
  no desugaring and no `coreLibraryDesugaring`.
- Parts we do not edit are copied byte for byte, which a test asserts across the
  whole package. Only the edited sheet part is re-serialized.
- Re-serializing the edited sheet normalizes attribute order and empty-element
  whitespace. The XML is semantically identical and the dropdowns, styles,
  frozen panes and formulas survive, but the edited part is not byte-identical.
  Tests assert semantics, not bytes, for that one part.
- We write text as an inline string (`t="inlineStr"`) so `xl/sharedStrings.xml`
  never changes. Readers must handle shared, inline and numeric cells, which the
  reader does.
- We own the format knowledge. Anything beyond "read cells, write cells" —
  formulas, merged ranges, new rows — is work we do ourselves, and the reader
  loads the sheet into a DOM, which is fine for a section-sized template and
  would not be for a large workbook.

## Risks

- The fixture is modelled from the Minedu instructives in `docs/siagie/`, not
  from a file SIAGIE generated. A real template needs a teacher login.
- SIAGIE's own importer may reject `t="inlineStr"` if it reads cells strictly.
  The fallback is to append to `xl/sharedStrings.xml` and write `t="s"`, which
  keeps every other guarantee and is the only change that fallback requires.
- Neither risk closes without one real template uploaded once through SIAGIE.
