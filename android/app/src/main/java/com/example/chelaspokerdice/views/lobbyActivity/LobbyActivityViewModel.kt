package com.example.chelaspokerdice.views.lobbyActivity

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chelaspokerdice.domain.Game
import com.example.chelaspokerdice.services.GameServiceInterface
import com.example.chelaspokerdice.services.PlayerServiceInterface
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


sealed interface GameViewState {
    data class WaitingForGameStart(val game: Game) : GameViewState
    data class InGame(val game: Game) : GameViewState
    data class GameOver(val game: Game) : GameViewState
    data class Error(val error: String) : GameViewState
    data class TurnTimedOut(val game: Game) : GameViewState
    data class RoundComplete(val game: Game) : GameViewState
    data class WaitingForAnte(val game: Game) : GameViewState
}


class LobbyActivityViewModel(
    userService: PlayerServiceInterface,
    private val gameService: GameServiceInterface
): ViewModel() {

    var screenState by mutableStateOf<GameViewState?>(null)
    var game by mutableStateOf<Game?>(null)

    val user = userService.currentUser.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    fun setGameState(newGameState: Game?) {
        if (newGameState == null) screenState = GameViewState.Error("No game data provided")
        else {
            game = newGameState
            when {
                newGameState.gameStateType == "InLobby" -> {
                    screenState = GameViewState.WaitingForGameStart(game!!)
                }
                newGameState.gameStateType == "WaitingForPlayerAction" -> {
                    screenState = GameViewState.InGame(game!!)
                }
                newGameState.gameStateType == "TurnTimedOut" -> {
                    screenState = GameViewState.TurnTimedOut(game!!)
                }
                newGameState.gameStateType == "GameOver" -> {
                    screenState = GameViewState.GameOver(game!!)
                }
                newGameState.gameStateType == "RoundComplete" -> {
                    screenState = GameViewState.RoundComplete(game!!)
                }
                newGameState.gameStateType == "WaitingForAnte" -> {
                    screenState = GameViewState.WaitingForAnte(game!!)
                }
                else -> {
                    screenState = GameViewState.Error("Unknown game state: ${newGameState.gameStateType}")
                }
            }
        }
    }

    fun leaveLobby(gameId: Int) {
        Log.d("LEAVE CALLED", "About to call leaveGame with gameId: $gameId and user: ${user.value}")
        viewModelScope.launch {
            if (user.value == null) {
                Log.e("Leave Lobby", "User is null, cannot leave game")
                return@launch
            }
            gameService.leaveGame(
                gameId,
                user.value!!.token
            )
        }
        Log.d("Leave Lobby","Leave Lobby called with lobby id : $gameId")
    }

    fun startGame() {
        viewModelScope.launch {
            if (user.value == null) {
                Log.e("Start Game", "User is null, cannot start game")
                return@launch
            }
            try {
                val updatedGame = gameService.startGame(
                    game!!.id,
                    user.value!!.token
                )
                if (updatedGame != null) {
                    Log.d("Start Game", "Game started successfully: $game")
                    game = updatedGame
                    setGameState(game)
                }
            } catch (e: Exception) {
                Log.e("Start Game", "Failed to start game!!@#!@#: ${e.message}")
                return@launch
            }
        }
        Log.d("Start Game","Start Game called with game id : ${game!!.id}")
    }

    fun placeBet(ante: Int) {
        viewModelScope.launch {
            try {
                val updatedGame = gameService.bet(
                    game!!.id,
                    user.value!!.token,
                    ante
                )
                Log.d("Place Bet", "Bet placed successfully: $updatedGame")
                game = updatedGame
                setGameState(game)
            } catch (e: Exception) {
                Log.e("Place Bet", "Failed to place bet: ${e.message}")
                return@launch
            }
        }
        Log.d("Place Bet","Place Bet called with game id : ${game!!.id} and amount: $ante")
    }

    fun endTurn() {
        viewModelScope.launch {
            try {
                val updatedGame = gameService.endTurn(
                    game!!.id,
                    user.value!!.token
                )
                Log.d("End Turn", "Turn ended successfully: $updatedGame")
                game = updatedGame
                setGameState(game)
            } catch (e: Exception) {
                Log.e("End Turn", "Failed to end turn: ${e.message}")
                return@launch
            }
        }
        Log.d("End Turn","End Turn called with game id : ${game!!.id}")
    }

    fun rollDices(dices: List<Int>) {
        viewModelScope.launch {
            try {
                val updatedGame = gameService.rollDice(
                    game!!.id,
                    user.value!!.token,
                    dices
                )
                Log.d("Roll Dices", "Dices rolled successfully: $updatedGame")
                game = updatedGame
                setGameState(game)
            } catch (e: Exception) {
                Log.e("Roll Dices", "Failed to roll dices: ${e.message}")
                return@launch
            }
        }
        Log.d("Roll Dices","Roll Dices called with game id : ${game!!.id} and dices: $dices")
    }

    fun nextRound() {
        viewModelScope.launch {
            try {
                val updatedGame = gameService.nextRound(
                    game!!.id,
                    user.value!!.token
                )
                Log.d("Next Round", "Proceeded to next round successfully: $updatedGame")
                game = updatedGame
                setGameState(game)
            } catch (e: Exception) {
                Log.e("Next Round", "Failed to proceed to next round: ${e.message}")
                return@launch
            }
        }
        Log.d("Next Round","Next Round called with game id : ${game!!.id}")
    }

    fun listenForGameUpdates() {
        Log.d("Game Updates", "Setting up listener for game updates...")
        viewModelScope.launch {
            try {
                val gameUpdatesFlow = gameService.getGameStateUpdates(game!!.id)
                gameUpdatesFlow.collect { updatedGame ->
                    Log.d("Game Updates", "Received game update: $updatedGame")
                    game = updatedGame
                    setGameState(game)
                }
            } catch (e: Exception) {
                Log.e("Game Updates", "Failed to listen for game updates: ${e.message}")
            }
        }
        Log.d("Game Updates","Listening for game updates for game id : ${game!!.id}")
    }

    fun getCurrentGameState() {
        viewModelScope.launch {
            try {
                val currentGame = gameService.getGameState(
                    game!!.id,
                    user.value!!.token
                )
                Log.d("Get Current Game State", "Fetched current game state successfully: $currentGame")
                game = currentGame
                setGameState(game)
            } catch (e: Exception) {
                Log.e("Get Current Game State", "Failed to fetch current game state: ${e.message}")
                return@launch
            }
        }
        Log.d("Get Current Game State","Get Current Game State called with game id : ${game!!.id}")
    }
}