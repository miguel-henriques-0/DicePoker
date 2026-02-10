package pt.isel.daw.dicepoker.repository

import pt.isel.daw.dicepoker.domain.games.Game
import pt.isel.daw.dicepoker.domain.games.GameState
import pt.isel.daw.dicepoker.domain.games.GameStatus
import pt.isel.daw.dicepoker.domain.games.UserGame

interface GamesRepository {
    // CRUD Operations for game

    /*
     * 1 - List N_Games by State
     * 2 - List N_Games where user is playing
     * 3 - Create a N_Game
     * 4 - Delete a N_Game by id
     * 5 - Update N_Game state by id
     * 6 - Get N_Game by id
     */

    // 3
    fun createGame(
        name: String,
        description: String,
        rounds: Int,
        maxPlayers: Int,
        minPlayers: Int,
        // time for game to start when minPlayers achieved and !maxPlayers
        timeout: Long,
        userId: Int,
        username: String,
        gameState: GameState,
    ): Game

    // 4
    fun deleteGame(gameId: Int)

    // 5
    fun updateGame(
        gameId: Int,
        gameStatus: GameStatus? = null,
        gameState: GameState,
    ): Game

    fun updateGameStatus(
        gameId: Int,
        status: GameStatus,
    ): Game

    // 6
    fun getGameById(gameId: Int): Game?

    // 1
    fun listGames(
        order: String,
        limit: Int,
        status: String,
        lastGameId: Int?,
    ): List<Game>

    // 2
    fun getUserGame(userId: Int): List<Game>

    fun joinGame(
        gameId: Int,
        userId: Int,
    )

    fun getGameHost(
        gameId: Int,
        userId: Int,
    ): Boolean

    fun leaveGame(
        gameId: Int,
        userId: Int,
    )

    fun leaveHostGame(
        gameId: Int,
        userId: Int,
    )

    fun getPlayersWaiting(gameId: Int): List<UserGame>

    fun updateUserGame(
        gameId: Int,
        userId: Int,
    )
}
