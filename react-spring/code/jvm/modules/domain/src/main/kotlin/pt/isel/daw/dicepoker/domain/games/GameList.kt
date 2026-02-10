package pt.isel.daw.dicepoker.domain.games

data class GameList(
    val games: List<Game>,
    val lastGameId: Int?,
)
