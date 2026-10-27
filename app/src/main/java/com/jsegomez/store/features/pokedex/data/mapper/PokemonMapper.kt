package com.jsegomez.store.features.pokedex.data.mapper

import com.jsegomez.store.features.pokedex.data.remote.dto.PokemonDetailDto
import com.jsegomez.store.features.pokedex.data.remote.dto.PokemonListDto
import com.jsegomez.store.features.pokedex.data.remote.dto.PokemonSummaryDto
import com.jsegomez.store.features.pokedex.domain.model.PokemonAbility
import com.jsegomez.store.features.pokedex.domain.model.PokemonDetail
import com.jsegomez.store.features.pokedex.domain.model.PokemonStat
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary

private const val ARTWORK_URL =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"

fun PokemonListDto.toDomain(): List<PokemonSummary> =
    results.mapNotNull { it.toDomain() }

private fun PokemonSummaryDto.toDomain(): PokemonSummary? {
    val id = url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: return null
    return PokemonSummary(id = id, name = name, imageUrl = artworkUrl(id))
}

fun PokemonDetailDto.toDomain(): PokemonDetail =
    PokemonDetail(
        id = id,
        name = name,
        imageUrl = artworkUrl(id),
        heightMeters = height / 10.0,
        weightKg = weight / 10.0,
        types = types.sortedBy { it.slot }.map { it.type.name },
        abilities = abilities.map { PokemonAbility(it.ability.name, it.isHidden) },
        stats = stats.map { PokemonStat(it.stat.name, it.baseStat) },
    )

private fun artworkUrl(id: Int): String = "$ARTWORK_URL$id.png"