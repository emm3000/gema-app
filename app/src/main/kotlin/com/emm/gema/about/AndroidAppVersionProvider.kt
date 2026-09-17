package com.emm.gema.about

import android.content.Context
import android.content.pm.PackageInfo

class AndroidAppVersionProvider(private val context: Context) : AppVersionProvider {

    override fun versionName(): String {
        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return packageInfo.versionName.orEmpty()
    }
}
