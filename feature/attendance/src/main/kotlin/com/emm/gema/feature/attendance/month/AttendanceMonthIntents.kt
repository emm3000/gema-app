package com.emm.gema.feature.attendance.month

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

private const val FILE_PROVIDER_SUFFIX: String = ".attendance"

fun Context.shareAttendanceExport(path: String, mimeType: String, chooserTitle: String) {
    val shared: Uri = FileProvider.getUriForFile(
        this,
        packageName + FILE_PROVIDER_SUFFIX,
        File(path),
    )
    val share = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, shared)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(Intent.createChooser(share, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
