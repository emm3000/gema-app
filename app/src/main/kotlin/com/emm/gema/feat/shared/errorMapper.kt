package com.emm.gema.feat.shared

fun normalizeErrorMessage(throwable: Throwable): String {
    return "Ocurrio un error inesperado, intenta nuevamente\n${throwable::class.simpleName}"
}