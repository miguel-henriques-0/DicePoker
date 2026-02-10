package com.example.chelaspokerdice.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class LobbyListDTO(
    val games: List<GameDTO>,
    val lastGameId: Int?
)