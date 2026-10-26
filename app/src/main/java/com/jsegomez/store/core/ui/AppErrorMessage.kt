package com.jsegomez.store.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jsegomez.store.R
import com.jsegomez.store.core.network.AppError

@Composable
fun AppError.toMessage(): String = when (this) {
    is AppError.NoConnection -> stringResource(R.string.error_no_connection)
    is AppError.Timeout -> stringResource(R.string.error_timeout)
    is AppError.Http -> stringResource(R.string.error_server, code)
    is AppError.Parsing, is AppError.Unknown -> stringResource(R.string.error_generic)
}