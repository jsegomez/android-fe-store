package com.jsegomez.store.di

import com.jsegomez.store.BuildConfig
import com.jsegomez.store.features.pokedex.data.remote.PokeApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val POKE_BASE_URL = "https://pokeapi.co/api/v2/"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
        redactHeader("Authorization")
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(POKE_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val pokeApi: PokeApi = retrofit.create(PokeApi::class.java)
}
