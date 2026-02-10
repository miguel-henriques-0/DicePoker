package com.example.chelaspokerdice.services

import android.util.Log
import com.example.chelaspokerdice.domain.Game
import com.example.chelaspokerdice.domain.Lobby
import com.example.chelaspokerdice.domain.dto.GameDTO
import com.example.chelaspokerdice.domain.dto.LobbyListDTO
import com.example.chelaspokerdice.domain.dto.toGame
import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow

import kotlinx.serialization.Serializable
import okhttp3.HttpUrl

class LobbyService(
    private val client: HttpClient
): LobbyServiceInterface {
    override suspend fun createLobby(
        lobby: Lobby,
        token: String
    ): Game {
        val response = client.post {
            url("http://10.0.2.2:8180/api/game/create")
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $token")
            }
            setBody(
                CreateLobbyRequest(
                    name = lobby.name,
                    description = lobby.description,
                    maxPlayers = lobby.maxPlayers,
                    rounds = lobby.rounds
                )
            )
        }
        Log.d("#### TESTE #####", "@@@@@@@@@")

        if (response.status != HttpStatusCode.Created) {
            Log.d("LobbyService", "Failed to create lobby: ${response.status}")
            throw Exception("Failed to create lobby: ${response.status}")
        }
        Log.d("LobbyService", "Lobby created successfully - $response")
        val gameDTO = response.body<GameDTO>()
        return gameDTO.toGame()
    }

    override suspend fun getLobbies(token: String, lastGameId: Int?): LobbyListDTO {
        val url = HttpUrl.Builder()
            .scheme("http")
            .host("10.0.2.2")
            .port(8180)
            .addPathSegments("api/game/list")
            .addQueryParameter(
                "lastGameId",
                (lastGameId ?: 0).toString(),
            )
            .addQueryParameter(
                "limit",
                "20"
            )
            .build().toString()
        Log.d("LobbyService", "Constructed URL: $url")
        val response = client.get {
//            url("http://10.0.2.2:8180/api/game/list&lastGameId=${lastGameId ?: 0}")
            url(url)
            Log.d("LobbyService", "Fetching lobbies with lastGameId: ${lastGameId ?: 0}")
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $token")
            }
        }

        if (response.status != HttpStatusCode.OK) {
            Log.d("LobbyService", "Failed to create lobby: ${response.status}")
            throw Exception("Failed to create lobby: ${response.status}")
        }
        Log.d("LobbyService", "Lobby created successfully - $response")
        val lobbyList = response.body<LobbyListDTO>()
        return lobbyList
    }

    override suspend fun joinLobby(lobbyId: Int, token: String): Game {
        val response = client.put {
            url("http://10.0.2.2:8180/api/game/$lobbyId/join")
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $token")
            }
        }

        if (response.status != HttpStatusCode.OK) {
            Log.d("LobbyService", "Failed to join lobby: ${response.status}")
            throw Exception("Failed to join lobby: ${response.status}")
        }
        Log.d("LobbyService", "Joined lobby successfully - $response")
        val gameDTO = response.body<GameDTO>()
        return gameDTO.toGame()
    }
}


@Serializable
data class CreateLobbyRequest(
    val name: String,
    val description: String,
    val maxPlayers: Int,
    val rounds: Int,
)

