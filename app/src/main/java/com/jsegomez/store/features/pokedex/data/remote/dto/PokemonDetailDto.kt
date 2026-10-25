package com.jsegomez.store.features.pokedex.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    @SerialName("base_experience") val baseExperience: Int?,
    val types: List<TypeSlotDto>,
    val abilities: List<AbilitySlotDto>,
    val stats: List<StatSlotDto>,
    val sprites: SpritesDto,
)

@Serializable
data class NamedResourceDto(
    val name: String,
    val url: String,
)

@Serializable
data class TypeSlotDto(
    val slot: Int,
    val type: NamedResourceDto,
)

@Serializable
data class AbilitySlotDto(
    val ability: NamedResourceDto,
    @SerialName("is_hidden") val isHidden: Boolean,
    val slot: Int,
)

@Serializable
data class StatSlotDto(
    @SerialName("base_stat") val baseStat: Int,
    val effort: Int,
    val stat: NamedResourceDto,
)

@Serializable
data class SpritesDto(
    @SerialName("front_default") val frontDefault: String? = null,
)
