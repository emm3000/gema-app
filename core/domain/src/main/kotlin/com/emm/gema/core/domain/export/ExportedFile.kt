package com.emm.gema.core.domain.export

const val SIAGIE_GRADES_MIME_TYPE: String =
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

data class ExportedFile(
    val name: String,
    val path: String,
) {
    init {
        require(name.isNotBlank()) { "An exported file keeps the name SIAGIE gave it" }
        require(path.isNotBlank()) { "An exported file needs a path" }
    }
}
