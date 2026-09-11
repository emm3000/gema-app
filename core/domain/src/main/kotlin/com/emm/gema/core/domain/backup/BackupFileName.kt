package com.emm.gema.core.domain.backup

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val BACKUP_FILE_EXTENSION: String = ".gema"
const val BACKUP_MIME_TYPE: String = "application/octet-stream"

private val fileNameFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")

fun backupFileName(createdAt: LocalDateTime): String =
    "gema-${fileNameFormat.format(createdAt)}$BACKUP_FILE_EXTENSION"
