package com.jsegomez.store.di

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object NetworkModule{
    private const val POKE_BASE_URL = "https://pokeapi.co/api/v2/"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
}


