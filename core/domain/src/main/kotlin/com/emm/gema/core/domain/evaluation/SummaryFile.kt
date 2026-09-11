package com.emm.gema.core.domain.evaluation

data class SummaryFile(
    val name: String,
    val path: String,
) {
    init {
        require(name.isNotBlank()) { "A summary file needs a name" }
        require(path.isNotBlank()) { "A summary file needs a path" }
    }
}
