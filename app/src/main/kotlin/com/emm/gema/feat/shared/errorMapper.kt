package com.emm.gema.feat.shared

import retrofit2.HttpException

fun normalizeErrorMessage(throwable: Throwable): String {
    if (throwable is HttpException) return """Ocurrio un error inesperado, intenta nuevamente
        ${throwable.code()}: ${throwable.message()}
        ${throwable.response()?.errorBody()?.string()}
    """.trimIndent()
    return "Ocurrio un error inesperado, intenta nuevamente\n${throwable::class.simpleName}"
}