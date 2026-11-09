package com.jsegomez.store.core.network

sealed class AppError(cause: Throwable? = null) : Exception(cause) {
    class NoConnection(cause: Throwable) : AppError(cause)
    class Timeout(cause: Throwable) : AppError(cause)
    class Http(val code: Int, cause: Throwable) : AppError(cause)
    class Parsing(cause: Throwable) : AppError(cause)
    class Unknown(cause: Throwable) : AppError(cause)
}

// Convierte cualquier error en un AppError para poder mostrarlo en pantalla.
// Si ya es un AppError (el caso normal, lo produce safeApiCall) se deja tal cual.
fun Throwable.toAppError(): AppError = this as? AppError ?: AppError.Unknown(this)