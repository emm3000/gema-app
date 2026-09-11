package com.emm.gema.feature.backup

import android.content.Context
import android.content.Intent
import kotlin.system.exitProcess

sealed interface RestartOutcome {
    data object Restarted : RestartOutcome
    data object ManualRestartRequired : RestartOutcome
}

fun Context.restart(): RestartOutcome {
    val launchIntent: Intent? = packageManager.getLaunchIntentForPackage(packageName)
    return performRestart(
        launch = launchIntent?.let { intent ->
            {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        },
        exit = { exitProcess(0) },
    )
}

internal fun performRestart(launch: (() -> Unit)?, exit: () -> Unit): RestartOutcome {
    if (launch == null) return RestartOutcome.ManualRestartRequired
    launch()
    exit()
    return RestartOutcome.Restarted
}
