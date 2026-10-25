package com.jsegomez.store.core.network

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e.toAppError())
    }

private fun Exception.toAppError(): AppError = when (this) {
    is SocketTimeoutException -> AppError.Timeout(this)
    is UnknownHostException, is ConnectException -> AppError.NoConnection(this)
    is HttpException -> AppError.Http(code(), this)
    is SerializationException -> AppError.Parsing(this)
    else -> AppError.Unknown(this)
}