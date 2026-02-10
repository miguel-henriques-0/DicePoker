package com.example.chelaspokerdice.services

import com.example.chelaspokerdice.domain.Game
import kotlinx.coroutines.flow.Flow

interface GameServiceInterface {
    suspend fun getGameState(gameId: Int, token: String): Game
    suspend fun rollDice(gameId: Int, token: String, listDices: List<Int>): Game
    suspend fun bet(gameId: Int, token: String, ante: Int): Game
    suspend fun endTurn(gameId: Int, token: String): Game
    suspend fun leaveGame(gameId: Int, token: String)
    suspend fun startGame(gameId: Int, token: String): Game?
    suspend fun nextRound(gameId: Int, token: String): Game

    suspend fun getGameStateUpdates(gameId: Int): Flow<Game>
}