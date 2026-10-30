package com.jsegomez.store.di

import com.jsegomez.store.BuildConfig
import com.jsegomez.store.features.pokedex.data.remote.PokeApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

// Un String suelto es ambiguo en Koin: el qualifier deja claro que esta URL es la de PokéAPI
// y evita choques cuando otra API (por ejemplo la de auth) registre su propia URL base
private val pokeBaseUrl = named("pokeBaseUrl")

val networkModule = module {
    single(pokeBaseUrl) { "https://pokeapi.co/api/v2/" }

    single {
        Json {
            ignoreUnknownKeys = true
        }
    }

    single {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                            redactHeader("Authorization")
                        }
                    )
                }
            }
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(get<String>(pokeBaseUrl))
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single { get<Retrofit>().create(PokeApi::class.java) }
}
