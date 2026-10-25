package com.jsegomez.store.features.pokedex.data.mapper

import com.jsegomez.store.features.pokedex.data.remote.dto.PokemonListDto
import com.jsegomez.store.features.pokedex.data.remote.dto.PokemonSummaryDto
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary

private const val ARTWORK_URL =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"

fun PokemonListDto.toDomain(): List<PokemonSummary> =
    results.mapNotNull { it.toDomain() }

private fun PokemonSummaryDto.toDomain(): PokemonSummary? {
    val id = url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: return null
    return PokemonSummary(id = id, name = name, imageUrl = "$ARTWORK_URL$id.png")
}