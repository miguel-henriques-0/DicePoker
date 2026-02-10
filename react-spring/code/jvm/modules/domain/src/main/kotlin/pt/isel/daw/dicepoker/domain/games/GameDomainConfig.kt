package pt.isel.daw.dicepoker.domain.games

data class GameDomainConfig(
    val minPlayers: Int,
    val maxPlayers: Int,
    val timeout: Long,
    val maxConcurrentGames: Int,
    val maxRollsPerTurn: Int,
    val turnTimer: Long,
    val handSize: Int,
    val initialBalance: Int,
) {
    init {
        require(minPlayers >= 2)
        require(maxPlayers <= 5)
        require(timeout > 0)
        require(maxConcurrentGames > 0)
        require(maxRollsPerTurn > 0)
        require(turnTimer > 0)
        require(handSize > 0)
        require(initialBalance >= 0)
    }
}
