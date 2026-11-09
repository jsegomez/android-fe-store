package com.jsegomez.store.features.pokedex.data.mapper

import com.jsegomez.store.features.pokedex.data.local.PokemonEntity
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary

// Conversión entre el modelo de dominio (lo que usa la app) y la entidad de Room (lo que se guarda).
// Se mantienen separados para que cambiar la base de datos no obligue a tocar la UI, y viceversa.
// Las conversiones desde la API (DTO) viven aparte, en PokemonMapper.kt.

fun PokemonSummary.toEntity(): PokemonEntity =
    PokemonEntity(id = id, name = name, imageUrl = imageUrl)

fun PokemonEntity.toDomain(): PokemonSummary =
    PokemonSummary(id = id, name = name, imageUrl = imageUrl)
