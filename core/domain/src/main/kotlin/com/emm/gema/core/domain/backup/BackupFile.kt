package com.emm.gema.core.domain.backup

data class BackupFile(
    val name: String,
    val path: String,
) {
    init {
        require(name.isNotBlank()) { "A backup file needs a name" }
        require(path.isNotBlank()) { "A backup file needs a path" }
    }
}
