package com.jsegomez.store.di

import androidx.room.Room
import com.jsegomez.store.features.pokedex.data.local.MIGRATION_1_2
import com.jsegomez.store.features.pokedex.data.local.PokedexDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    // single: una sola instancia de la base para toda la app. Crear varias abriría varias
    // conexiones al mismo archivo y los observadores de una no verían los cambios de otra.
    single {
        Room.databaseBuilder(androidContext(), PokedexDatabase::class.java, PokedexDatabase.NAME)
            // Cada migración nueva se agrega aquí. Sin ella, Room no sabría actualizar una base ya
            // instalada y la app se cerraría al abrirla.
            // NUNCA usar fallbackToDestructiveMigration(): borraría las fichas que el usuario ya descargó.
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    // Los DAO se obtienen de la base; así los repositorios reciben solo el DAO que necesitan.
    single { get<PokedexDatabase>().pokemonDao() }
    single { get<PokedexDatabase>().detailDao() }
}
