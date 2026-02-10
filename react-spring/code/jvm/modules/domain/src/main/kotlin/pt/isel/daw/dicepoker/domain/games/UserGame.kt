package pt.isel.daw.dicepoker.domain.games

data class UserGame(
    val gameId: Int,
    val userId: Int,
    val isHost: Boolean,
    val hasLeft: Boolean,
)
