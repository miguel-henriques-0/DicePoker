package com.example.chelaspokerdice.views.lobbyListActivity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chelaspokerdice.domain.Game
import com.example.chelaspokerdice.domain.User
import com.example.chelaspokerdice.domain.dto.toGame
import com.example.chelaspokerdice.services.LobbyServiceInterface
import com.example.chelaspokerdice.services.PlayerServiceInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LobbyListViewModel(
    private val lobbyService: LobbyServiceInterface,
    userService: PlayerServiceInterface
): ViewModel() {
    // private for local modification only
    private val gamesList = MutableStateFlow<List<Game>>(emptyList())
    // For infinite scrolling
    val lastGameId = MutableStateFlow<Int?>(null)
    // public access for immmutability
    val games: StateFlow<List<Game>> = gamesList.asStateFlow()

    val user: StateFlow<User?> = userService.currentUser.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )
    // Initialize by fetching the first lobbies
    init {
        fetchLobbies()
    }

    fun fetchLobbies() {
        viewModelScope.launch {
            try {
                val token = user.value?.token ?: "ERROR_NO_TOKEN"
                val lobbies = lobbyService.getLobbies(token, lastGameId.value)
                val newGames = lobbies.games.map { it.toGame() }
                gamesList.value = gamesList.value + newGames
                lastGameId.value = lobbies.lastGameId
                Log.d("LobbyListViewModel", "Fetched ${gamesList.value.size} lobbies. Last Game ID: ${lobbies.lastGameId ?: 0}")
                Log.d("LobbyListViewModel", "Last Game ID value: ${lastGameId.value ?: 0}")
            } catch (e: Exception) {
                Log.e("LobbyListViewModel", "Error fetching lobbies: ${e.message}")
            }
        }
    }

    fun joinGame(gameId: Int) {
        viewModelScope.launch {
            try {
                val token = user.value?.token ?: "ERROR_NO_TOKEN"
                lobbyService.joinLobby(gameId, token)
            } catch (e: Exception) {
                Log.e("LobbyListViewModel", "Error joining game: ${e.message}")
            }
        }
    }

    fun refreshLobbies() {
        gamesList.value = emptyList()
        lastGameId.value = null
        fetchLobbies()
    }
}
