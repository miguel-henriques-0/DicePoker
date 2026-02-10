package pt.isel.daw.dicepoker.domain.games

import kotlinx.datetime.Clock
import org.springframework.stereotype.Component
import pt.isel.daw.dicepoker.domain.players.PlayerSummary

@Component
class GameDomain(
    private val config: GameDomainConfig,
) {
    val lobbyTimeout: Long = config.timeout
    val handSize: Int = config.handSize

    fun checkMaxConcurrentGamesReached(currentGames: Int): Boolean = currentGames < config.maxConcurrentGames

    fun isFull(
        maxPlayers: Int,
        currentPlayers: Int,
    ): Boolean = currentPlayers + 1 > maxPlayers

    fun canStartGame(
        maxPlayers: Int,
        minPlayers: Int,
        currentPlayers: Int,
        createdAt: Long,
    ): Boolean =
        (currentPlayers >= minPlayers && Clock.System.now().epochSeconds >= createdAt + config.timeout) || currentPlayers == maxPlayers

    fun isGameHost(
        playersInGame: List<UserGame>,
        userId: Int,
    ): Boolean = playersInGame.find { it.userId == userId && it.isHost } != null

    fun createPlayerSummary(listPlayersInGame: List<UserGame>): List<PlayerSummary> {
        val summaryList = mutableListOf<PlayerSummary>()
        listPlayersInGame.forEach { playerInGame ->
            summaryList.add(
                PlayerSummary(
                    userId = playerInGame.userId,
                    balance = config.initialBalance,
                    isHost = playerInGame.isHost,
                ),
            )
        }
        return summaryList
    }

    fun canLeaveGame(game: Game): Boolean {
        return when (GameStatus.valueOf(game.status)) {
            // Can leave if game status is set to Open or Playing
            GameStatus.CLOSED, GameStatus.FINISHED -> {
                false
            }
            else -> {
                true
            }
        }
    }

    fun isUserInGame(
        playersInGame: List<UserGame>,
        userId: Int,
    ) = playersInGame.find { it.userId == userId && !it.hasLeft } != null

    fun gameAlreadyInProgress(game: Game): Boolean = game.status == GameStatus.PLAYING.name
}
