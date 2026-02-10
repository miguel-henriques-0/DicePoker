package pt.isel.daw.dicepoker.http.model.game

class GameCreateInputModel(
    val name: String,
    val description: String,
    val rounds: Int,
    val maxPlayers: Int?,
    val minPlayers: Int?,
    val timeout: Long?,
)
