package com.example.chelaspokerdice.services

import com.example.chelaspokerdice.domain.Game
import com.example.chelaspokerdice.domain.Lobby
import com.example.chelaspokerdice.domain.dto.LobbyListDTO
import kotlinx.coroutines.flow.Flow

interface LobbyServiceInterface {
    suspend fun createLobby(lobby: Lobby, token: String): Game
    suspend fun getLobbies(token: String, lastGameId: Int?): LobbyListDTO
    suspend fun joinLobby(lobbyId: Int, token: String): Game
}