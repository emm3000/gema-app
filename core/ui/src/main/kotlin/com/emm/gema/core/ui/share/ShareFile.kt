package com.emm.gema.core.ui.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

private const val FILE_PROVIDER_AUTHORITY_SUFFIX: String = ".shared_files"

data class ShareIntentSpec(
    val action: String,
    val mimeType: String,
    val streamUri: String,
    val flags: Int,
    val chooserTitle: String,
)

fun buildShareIntentSpec(streamUri: String, mimeType: String, chooserTitle: String): ShareIntentSpec = ShareIntentSpec(
    action = Intent.ACTION_SEND,
    mimeType = mimeType,
    streamUri = streamUri,
    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION,
    chooserTitle = chooserTitle,
)

fun Context.shareFile(file: File, mimeType: String, chooserTitle: String) {
    val authority: String = packageName + FILE_PROVIDER_AUTHORITY_SUFFIX
    val uri: Uri = FileProvider.getUriForFile(this, authority, file)
    val spec: ShareIntentSpec = buildShareIntentSpec(uri.toString(), mimeType, chooserTitle)
    val share: Intent = Intent(spec.action).apply {
        type = spec.mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(spec.flags)
    }
    startActivity(Intent.createChooser(share, spec.chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
