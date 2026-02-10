package pt.isel.daw.dicepoker.http.model.game

import pt.isel.daw.dicepoker.domain.games.GameState

data class GameCreateOutputModel(
    val id: Int,
    val name: String,
    val description: String,
    val rounds: Int,
    val status: String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val timeout: Long,
    val gameState: GameState?,
)
