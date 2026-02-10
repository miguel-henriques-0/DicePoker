package pt.isel.daw.dicepoker.domain.games

data class Game(
    val id: Int,
    val name: String,
    val description: String,
    val rounds: Int,
    val status: String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val timeout: Long,
    val createdAt: Long,
    val updatedAt: Long? = null,
    val gameStateType: String,
    val gameState: GameState? = null,
)
