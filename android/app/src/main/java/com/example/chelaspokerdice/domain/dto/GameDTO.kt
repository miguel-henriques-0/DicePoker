package com.example.chelaspokerdice.domain.dto
import com.example.chelaspokerdice.domain.Game
import kotlinx.serialization.Serializable

@Serializable
data class GameDTO(
    val id: Int,
    val name: String,
    val description: String,
    val rounds: Int,
    val status: String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val timeout: Long,
    val gameStateType: String,
    val gameState: GameStateDTO,
)

fun GameDTO.toGame(): Game {
    return Game(
        id = this.id,
        name = this.name,
        description = this.description,
        rounds = this.rounds,
        status = this.status,
        minPlayers = this.minPlayers,
        maxPlayers = this.maxPlayers,
        timeout = this.timeout,
        gameStateType = this.gameStateType,
        gameState = this.gameState.toGameState()
    )
}