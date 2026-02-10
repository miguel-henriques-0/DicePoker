package com.example.chelaspokerdice.services

import android.util.Log
import com.example.chelaspokerdice.domain.Game
import com.example.chelaspokerdice.domain.PlayAction
import com.example.chelaspokerdice.domain.dto.GameDTO
import com.example.chelaspokerdice.domain.dto.toGame
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readUTF8Line

class GameService(
    private val httpClient: HttpClient
): GameServiceInterface {

    object ApiConstants {
        const val PORT = "8180"
        const val GAME_BASE_URL = "http://10.0.2.2:$PORT/api/game/"
        const val SSE_URL = "http://10.0.2.2:$PORT/api/events/listen/"
    }

    override suspend fun getGameState(gameId: Int, token: String): Game {
        val res = httpClient.get {
            url("${ApiConstants.GAME_BASE_URL}$gameId")
            headers {
                append("Authorization", "Bearer $token")
            }
        }
        if (res.status.value == 200) {
            val gameDTO: GameDTO = res.body<GameDTO>()
            return gameDTO.toGame()
        } else {
            Log.e("GameService", "Failed to get game state: ${res.status}")
            throw Exception("Failed to get game state: ${res.status}")
        }
    }

    override suspend fun rollDice(
        gameId: Int,
        token: String,
        listDices: List<Int>
    ): Game {
        val res = httpClient.put {
            url("${ApiConstants.GAME_BASE_URL}$gameId/play")
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $token")
            }
            setBody(PlayRequest(
                dices = listDices,
                bet = null,
                playAction = PlayAction.ROLL.name
            ))
        }
        if (res.status.value == 200) {
            val gameDTO: GameDTO = res.body<GameDTO>()
            return gameDTO.toGame()
        } else {
            Log.e("GameService", "Failed to roll dice: ${res.status}")
            throw Exception("Failed to roll dice: ${res.status}")
        }
    }

    override suspend fun bet(gameId: Int, token: String, ante: Int): Game {
        val res = httpClient.put {
            url("${ApiConstants.GAME_BASE_URL}$gameId/play")
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $token")
            }
            setBody(PlayRequest(
                dices = null,
                bet = ante,
                playAction = PlayAction.BET.name
            ))
        }
        if (res.status.value == 200) {
            Log.d("GameService", "Bet placed successfully: $res")
            val gameDTO: GameDTO = res.body<GameDTO>()
            Log.d("GameService", "Updated game state after bet: $gameDTO")
            return gameDTO.toGame()
        } else {
            Log.e("GameService", "Failed to place bet: ${res.status}")
            throw Exception("Failed to place bet: ${res.status}")
        }
    }

    override suspend fun endTurn(gameId: Int, token: String): Game {
        val res = httpClient.put {
            url("${ApiConstants.GAME_BASE_URL}$gameId/play")
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $token")
            }
            setBody(PlayRequest(
                dices = null,
                bet = null,
                playAction = PlayAction.END.name
            ))
        }
        if (res.status.value == 200) {
            val gameDTO: GameDTO = res.body<GameDTO>()
            return gameDTO.toGame()
        } else {
            Log.e("GameService", "Failed to end turn: ${res.status}")
            throw Exception("Failed to end turn: ${res.status}")
        }
    }

    override suspend fun leaveGame(
        gameId: Int,
        token: String
    ) {
        val response = httpClient.put {
            url("${ApiConstants.GAME_BASE_URL}$gameId/leave")
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $token")
            }
        }

        if (response.status != HttpStatusCode.OK) {
            Log.d("LobbyService", "Failed to leave lobby: ${response.status}")
            throw Exception("Failed to leave lobby: ${response.status}")
        }
        Log.d("LobbyService", "Left lobby successfully - $response")
    }

    override suspend fun startGame(
        gameId: Int,
        token: String
    ): Game? {
        val res = httpClient.put {
            url("${ApiConstants.GAME_BASE_URL}$gameId/start")
            headers {
                append("Authorization", "Bearer $token")
            }
        }

        if (res.status.value == 200) {
            val gameDTO: GameDTO = res.body<GameDTO>()
            return gameDTO.toGame()
        } else if (res.status.value == 400) {
            val errorType: ProblemResponse = res.body()
            Log.e("GameService", "Cannot start game: Start conditions not met ${errorType.title}")
            return null
        } else {
            Log.e("GameService", "Failed to start game: ${res.status}")
            throw Exception("Failed to start game: ${res.status}")
        }
    }

    override suspend fun nextRound(
        gameId: Int,
        token: String
    ): Game {
        val res = httpClient.put {
            url("${ApiConstants.GAME_BASE_URL}$gameId/nextRound")
            headers {
                append("Authorization", "Bearer $token")
            }
        }

        if (res.status.value == 200) {
            val gameDTO: GameDTO = res.body<GameDTO>()
            return gameDTO.toGame()
        } else {
            Log.e("GameService", "Failed to start next round: ${res.status}")
            throw Exception("Failed to start next round: ${res.status}")
        }
    }

    override suspend fun getGameStateUpdates(gameId: Int): Flow<Game> = flow {
        try {
            httpClient.prepareGet {
                url("${ApiConstants.SSE_URL}$gameId")
            }.execute { response ->
                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    // if line continue else break
                    val line = channel.readUTF8Line() ?: break
                    Log.d("GameService", "SSE Line: $line")
                    when {
                        line.startsWith("data:") -> {
                            val dataLine = line.removePrefix("data:").trim()
                            Log.d("DATA SSE", "Data extracted: $dataLine")

                            try {
                                // First: parse SSE event
                                val sseEvent = json.decodeFromString<SSEEvent>(dataLine)

                                // Second: parse the nested JSON string to GameDTO
                                val gameDTO = json.decodeFromString<GameDTO>(sseEvent.msg)
                                emit(gameDTO.toGame())
                            } catch (e: Exception) {
                                Log.e("GameService", "Failed to parse SSE data: $e")
                            }
                        }
                        line.startsWith("event: ") -> {
                            val eventType = line.substring(7)
                            Log.d("GameService", "SSE Event: $eventType")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GameService", "Error in SSE connection: ${e.message}")
            throw e
        }
    }.flowOn(Dispatchers.IO)
}

@Serializable
data class PlayRequest(
    val dices: List<Int>?,
    val bet: Int?,
    val playAction: String
)

@Serializable
data class ProblemResponse(
    val type: String,
    val title: String
)

@Serializable
data class SSEEvent(
    val id: Int,
    val msg: String
)

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}
//https://stackoverflow.com/questions/44038721/constants-in-kotlin-whats-a-recommended-way-to-create-them