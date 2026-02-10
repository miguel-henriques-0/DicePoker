package com.example.chelaspokerdice.views.createLobbyActivity

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chelaspokerdice.domain.Game
import com.example.chelaspokerdice.domain.Lobby
import com.example.chelaspokerdice.domain.User
import com.example.chelaspokerdice.services.LobbyServiceInterface
import com.example.chelaspokerdice.services.PlayerServiceInterface
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CreateLobbyViewModel(
    private val lobbyService: LobbyServiceInterface,
    userService: PlayerServiceInterface
): ViewModel(
) {
    var screenState by mutableStateOf<CreateLobbyState>(CreateLobbyState.Idle)
    var expandedP by mutableStateOf(false)
    var expandedR by mutableStateOf(false)
    var lobbyName by mutableStateOf("")
    var description by mutableStateOf("")
    val numberOfPlayers = listOf("2", "3", "4", "5", "6")
    var selectedPlayer by mutableStateOf(numberOfPlayers[0])
    var lobbyNameError by mutableStateOf(false)
    var descriptionError by mutableStateOf(false)

    val roundsOptions = (2..60).filter {
        it % selectedPlayer.toInt() == 0
    }.map { it.toString() }

    var selectedRounds by mutableStateOf(roundsOptions[0])

    val player = userService.currentUser.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        null
    )

    fun createLobby(
        name: String,
        description: String,
        maxPlayers: Int,
        rounds: Int,
        player: User
    ) {
        viewModelScope.launch {
            try {
                screenState = CreateLobbyState.Loading
                val lobby = Lobby(
                    name = name,
                    description = description,
                    maxPlayers = maxPlayers,
                    rounds = rounds,
                    host = player
                )
                val newGame = lobbyService.createLobby(
                    lobby = lobby,
                    token = player.token
                )

                screenState = CreateLobbyState.Success(newGame)
            } catch (e: Exception) {
                screenState = CreateLobbyState.Error(e)
                Log.d("CreateLobbyViewModel", "Error creating lobby: ${e.message}")
            }
        }
    }

    sealed interface CreateLobbyState {
        data class Success(val game: Game) : CreateLobbyState
        data class Error(val error: Exception) : CreateLobbyState
        data object Loading : CreateLobbyState
        data object Idle : CreateLobbyState
    }

}