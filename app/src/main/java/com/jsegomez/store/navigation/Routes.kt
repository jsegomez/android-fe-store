package com.jsegomez.store.navigation

import kotlinx.serialization.Serializable

// Subgrafos de la app: identifican un grupo de pantallas, no son pantallas.
// Las rutas de cada pantalla viven en su feature (features/<x>/navigation/).
@Serializable data object Auth
@Serializable data object Main
