package com.jsegomez.store.features.pokedex.data.sync

import com.jsegomez.store.core.network.AppError
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

// Qué errores merecen un nuevo intento y cuáles no. Es la regla que usa PokeSyncWorker.
class RetryPolicyTest {
    private val cause = IOException()

    @Test
    fun networkProblems_areRetryable() {
        assertTrue(AppError.NoConnection(cause).isRetryable())
        assertTrue(AppError.Timeout(cause).isRetryable())
    }

    @Test
    fun http429_andServerErrors_areRetryable() {
        assertTrue(AppError.Http(429, cause).isRetryable())
        assertTrue(AppError.Http(500, cause).isRetryable())
        assertTrue(AppError.Http(503, cause).isRetryable())
        assertTrue(AppError.Http(599, cause).isRetryable())
    }

    @Test
    fun otherHttpErrors_arePermanent() {
        assertFalse(AppError.Http(400, cause).isRetryable())
        assertFalse(AppError.Http(404, cause).isRetryable())
        // Fronteras: justo debajo y justo encima del rango 500..599
        assertFalse(AppError.Http(499, cause).isRetryable())
        assertFalse(AppError.Http(600, cause).isRetryable())
    }

    @Test
    fun incompatibleJson_andUnknownErrors_arePermanent() {
        // Repetir no arregla un contrato roto, y un error desconocido no se debe insistir a ciegas
        assertFalse(AppError.Parsing(cause).isRetryable())
        assertFalse(AppError.Unknown(cause).isRetryable())
    }
}
