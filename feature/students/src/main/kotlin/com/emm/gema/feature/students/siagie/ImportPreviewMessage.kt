package com.emm.gema.feature.students.siagie

sealed interface ImportPreviewMessage {

    data object ImportFailed : ImportPreviewMessage

    data class Applied(val created: Int, val updated: Int, val withdrawn: Int) : ImportPreviewMessage
}
