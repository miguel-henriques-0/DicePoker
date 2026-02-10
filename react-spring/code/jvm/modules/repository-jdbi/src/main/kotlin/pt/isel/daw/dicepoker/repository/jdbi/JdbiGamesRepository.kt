package pt.isel.daw.dicepoker.repository.jdbi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kotlinx.datetime.Clock
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.core.statement.Update
import org.postgresql.util.PGobject
import pt.isel.daw.dicepoker.domain.games.Game
import pt.isel.daw.dicepoker.domain.games.GameOver
import pt.isel.daw.dicepoker.domain.games.GameState
import pt.isel.daw.dicepoker.domain.games.GameStatus
import pt.isel.daw.dicepoker.domain.games.InLobby
import pt.isel.daw.dicepoker.domain.games.RoundComplete
import pt.isel.daw.dicepoker.domain.games.TurnTimedOut
import pt.isel.daw.dicepoker.domain.games.UserGame
import pt.isel.daw.dicepoker.domain.games.WaitingForAnte
import pt.isel.daw.dicepoker.domain.games.WaitingForPlayerAction
import pt.isel.daw.dicepoker.repository.GamesRepository

class JdbiGamesRepository(
    private val handle: Handle,
) : GamesRepository {
    override fun createGame(
        name: String,
        description: String,
        rounds: Int,
        maxPlayers: Int,
        minPlayers: Int,
        timeout: Long,
        userId: Int,
        username: String,
        gameState: GameState,
    ): Game {
        val now = Clock.System.now()
        val status = GameStatus.OPEN

        val game =
            handle.createUpdate(
                """
                insert into dbo.games(name, description, rounds, status, min_players, max_players, timeout, created_at, updated_at, game_state_type, gameState)
                values (:name, :description, :rounds, :status, :min_players, :max_players, :timeout, :created_at, :updated_at, :currStateType, :gameState)
                """.trimIndent(),
            )
                .bind("name", name)
                .bind("description", description)
                .bind("rounds", rounds)
                .bind("status", status.toString())
                .bind("min_players", minPlayers)
                .bind("max_players", maxPlayers)
                .bind("timeout", timeout)
                .bind("created_at", now.epochSeconds)
                .bind("updated_at", now.epochSeconds)
                .bindGameState("gameState", gameState)
                .executeAndReturnGeneratedKeys()
                .mapTo<Game>()
                .one()

        handle.createUpdate(
            """
            INSERT INTO dbo.PlayerGame
            VALUES(:gameId, :userId, :is_host)
            """.trimIndent(),
        )
            .bind("gameId", game.id)
            .bind("userId", userId)
            // Whoever creates a game is always the host of said game
            .bind("is_host", true)
            .execute()

        return game
    }

    override fun deleteGame(gameId: Int) {
        TODO("Not yet implemented")
    }

    override fun updateGame(
        gameId: Int,
        gameStatus: GameStatus?,
        gameState: GameState,
    ): Game {
        return handle.createUpdate(
            """
            UPDATE dbo.games
            SET gameState = :gameState,
            status = :gameStatus,
            game_state_type = :currStateType
            WHERE id = :gameId
            RETURNING *
            """.trimIndent(),
        )
            .bindGameState("gameState", gameState)
            .bind("gameStatus", gameStatus.toString())
            .bind("gameId", gameId)
            .executeAndReturnGeneratedKeys()
            .mapTo<Game>()
            .one()
    }

    override fun updateGameStatus(
        gameId: Int,
        status: GameStatus,
    ): Game {
        return handle.createUpdate(
            """
            UPDATE dbo.games
            SET status = :status
            WHERE id = :gameId
            """.trimIndent(),
        )
            .bind("status", status.toString())
            .bind("gameId", gameId)
            .executeAndReturnGeneratedKeys()
            .mapTo<Game>()
            .one()
    }

    override fun getGameById(gameId: Int): Game? {
        return handle.createQuery(
            """
            SELECT * FROM dbo.games
            WHERE id = :gameId
            """.trimIndent(),
        )
            .bind("gameId", gameId)
            .mapTo<Game>()
            .singleOrNull()
    }

    override fun listGames(
        order: String,
        limit: Int,
        status: String,
        lastGameId: Int?,
    ): List<Game> {
        if (lastGameId != null) {
            return handle.createQuery(
                """
                    SELECT * FROM dbo.games
                    WHERE 
                        id > :lastGameId
                    AND
                        status = :status
                    ORDER BY :order
                    LIMIT :limit
                """.trimIndent()
            ).bind("lastGameId", lastGameId)
                .bind("status", status)
                .bind("order", order)
                .bind("limit", limit)
                .mapTo<Game>()
                .toList()
        } else {
            return handle.createQuery(
                """
                    SELECT * FROM dbo.games
                    WHERE status = :status
                    ORDER BY :order
                    LIMIT :limit
                """.trimIndent()
            ).bind("status", status)
                .bind("order", order)
                .bind("limit", limit)
                .mapTo<Game>()
                .toList()
        }
    }

    override fun getUserGame(userId: Int): List<Game> {
        return handle.createQuery(
            """
            SELECT * FROM dbo.games
            WHERE id IN (
            	SELECT game_id 
            	from dbo.playergame
            	where user_id = :userId
                and has_left = false
            );
            """.trimIndent(),
        )
            .bind("userId", userId)
            .mapTo<Game>()
            .toList()
    }

    override fun joinGame(
        gameId: Int,
        userId: Int,
    ) {
        // Join and return joined lobby
        handle.createUpdate(
            """
            INSERT INTO dbo.playergame
            VALUES(:gameId, :userId, false, false)
            """.trimIndent(),
        )
            .bind("gameId", gameId)
            .bind("userId", userId)
            .execute()
    }

    override fun getGameHost(
        gameId: Int,
        userId: Int,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun leaveGame(
        gameId: Int,
        userId: Int,
    ) {
        handle.createUpdate(
            """
            UPDATE dbo.playergame
            SET has_left = true
            WHERE game_id = :gameId
            and user_id = :userId
            """.trimIndent(),
        )
            .bind("gameId", gameId)
            .bind("userId", userId)
            .execute()
    }

    override fun leaveHostGame(
        gameId: Int,
        userId: Int,
    ) {
        handle.createUpdate(
            """
            UPDATE dbo.playergame
            SET has_left = true
            WHERE game_id = :gameId
            """.trimIndent(),
        )
            .bind("gameId", gameId)
            .execute()
    }

    override fun getPlayersWaiting(gameId: Int): List<UserGame> {
        return handle.createQuery(
            """
            SELECT * 
            FROM dbo.playergame
            WHERE game_id = :gameId
            """.trimIndent(),
        )
            .bind("gameId", gameId)
            .mapTo<UserGame>()
            .toList()
    }

    override fun updateUserGame(
        gameId: Int,
        userId: Int,
    ) {
        handle.createUpdate(
            """
            UPDATE dbo.playergame
            SET has_left = true
            WHERE user_id = :userId
            AND game_id = :gameId
            """.trimIndent(),
        )
            .bind("userId", userId)
            .bind("gameId", gameId)
            .execute()
    }

    companion object {
        private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

        private fun Update.bindGameState(
            name: String,
            gameState: GameState,
        ) = run {
            bind(
                name,
                PGobject().apply {
                    type = "jsonb"
                    value = serializeGameStateToJson(gameState)
                },
            )
            bind("currStateType", gameState::class.simpleName)
        }

        private fun serializeGameStateToJson(gameState: GameState): String = objectMapper.writeValueAsString(gameState)

        fun deserializeGameStateFromJson(
            json: String?,
            stateType: String?,
        ): GameState {
            if (stateType == null) {
                throw IllegalArgumentException("currStateType cannot be null")
            }

            return when (stateType) {
                "WaitingForAnte" -> objectMapper.readValue(json, WaitingForAnte::class.java)
                "RoundComplete" -> objectMapper.readValue(json, RoundComplete::class.java)
                "GameOver" -> objectMapper.readValue(json, GameOver::class.java)
                "TurnTimedOut" -> objectMapper.readValue(json, TurnTimedOut::class.java)
                "WaitingForPlayerAction" -> objectMapper.readValue(json, WaitingForPlayerAction::class.java)
                "InLobby" -> objectMapper.readValue(json, InLobby::class.java)
                else -> throw IllegalArgumentException("Unknown GameState type: $stateType")
            }
        }
    }
}
