package com.example.chelaspokerdice

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import android.util.Log
import com.example.chelaspokerdice.apiClient.UserAPIClient
import com.example.chelaspokerdice.commons.setAppLocale
import com.example.chelaspokerdice.services.DataStoreUserService
import com.example.chelaspokerdice.services.GameService
import com.example.chelaspokerdice.services.GameServiceInterface
import com.example.chelaspokerdice.services.LobbyServiceInterface
import com.example.chelaspokerdice.services.PlayerServiceInterface
import com.example.chelaspokerdice.services.LobbyService
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import com.example.chelaspokerdice.services.UserService
import kotlinx.serialization.json.Json
import kotlin.getValue

interface DependencyContainer {
    val userService: PlayerServiceInterface
    val lobbyService: LobbyServiceInterface
    val gameService: GameServiceInterface
}


class ChelasPokerDiceApp : Application(), DependencyContainer {
    val appDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    val httpClient by lazy {
        HttpClient {
            install(plugin = ContentNegotiation) {
                json(
                    json = Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    }
                )
            }
            install(Logging) {
                level = LogLevel.ALL
                logger = object : io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        Log.d("HTTP Client", message)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        setAppLocale("")
        Log.d("ChelasPokerDiceApp", "onCreate")
    }

    override val userService: PlayerServiceInterface by lazy {
        val dataStore = DataStoreUserService(appDataStore)
        UserService(dataStore, UserAPIClient(httpClient))
    }

    override val lobbyService: LobbyServiceInterface by lazy {
        LobbyService(client =httpClient)
    }

    override val gameService: GameServiceInterface by lazy {
        GameService(httpClient = httpClient)
    }
}