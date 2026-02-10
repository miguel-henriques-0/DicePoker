package com.example.chelaspokerdice.views.lobbyActivity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.chelaspokerdice.DependencyContainer
import com.example.chelaspokerdice.R
import com.example.chelaspokerdice.commons.viewModelInit
import com.example.chelaspokerdice.domain.Game
import com.example.chelaspokerdice.domain.PlayAction
import com.example.chelaspokerdice.ui.theme.ChelasPokerDiceTheme
import com.example.chelaspokerdice.views.BaseActivity
import com.example.chelaspokerdice.views.lobbyActivity.components.GameOverScreen
import com.example.chelaspokerdice.views.lobbyActivity.components.GameScreen
import com.example.chelaspokerdice.views.lobbyActivity.components.RoundCompleteScreen
import com.example.chelaspokerdice.views.lobbyActivity.components.TurnTimedOutScreen
import com.example.chelaspokerdice.views.lobbyActivity.components.WaitingLobbyScreen
import com.example.chelaspokerdice.views.lobbyActivity.components.WaitingForAnteScreen
import com.example.chelaspokerdice.views.lobbyListActivity.LobbyListActivity
import androidx.compose.runtime.collectAsState


class LobbyActivity: BaseActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vm by viewModels<LobbyActivityViewModel> {
            viewModelInit {
                LobbyActivityViewModel(
                    (application as DependencyContainer).userService,
                    (application as DependencyContainer).gameService,
                )
            }
        }

        Log.d("LobbyActivity", "onCreate called with ${vm.screenState ?: "null"}")

         // Initialize game state only if it's not already set
        if (vm.screenState == null) {
            val initialGame = intent.getParcelableExtra<Game>("game")
            Log.d("LobbyActivity", "Received game: $initialGame")
            vm.setGameState(initialGame)
            vm.listenForGameUpdates()
        }

        Log.d("VM STATE", vm.screenState.toString())
        Log.d("VM GAME", vm.game.toString())
        Log.d("VM USER", vm.user.value.toString())

        enableEdgeToEdge()
        setContent {
            ChelasPokerDiceTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("", color = Color.LightGray) },
                            actions = {
                                Button(
                                    onClick = {
                                        vm.leaveLobby(vm.game!!.id)
                                        navigate<LobbyListActivity> { intent ->
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        }
                                    },
                                    enabled = vm.screenState is GameViewState.WaitingForGameStart
                                            || vm.screenState is GameViewState.GameOver
                                ) {
                                    Text(stringResource(R.string.leave_button))
                                }
                            },
                            scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                        )
                    },
                ) { innerPadding ->
                    Log.d("What VM STATE 1", vm.screenState.toString())
                    val user = vm.user.collectAsState().value
                    if (user != null) {
                        when (vm.screenState) {
                            is GameViewState.WaitingForGameStart -> {
                                Log.d("What VM STATE 2", vm.screenState.toString())
                                WaitingLobbyScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    game = vm.game!!,
                                    onGameStart = {
                                        Log.d("LobbyActivity", "Game started callback triggered")
                                        vm.startGame()
                                    },
                                    user = vm.user.collectAsState().value!!
                                )
                            }

                            is GameViewState.InGame -> {
                                Log.d("What VM STATE 3", vm.screenState.toString())
                                Box {
                                    Text(" In Game")
                                }
                                GameScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    onPlay = { action ->
                                        when (action.action) {
                                            PlayAction.END -> {
                                                vm.endTurn()
                                            }

                                            PlayAction.ROLL -> {
                                                vm.rollDices(action.rollDices)
                                            }

                                            else -> {}
                                        }
                                    },
                                    vm = vm
                                )
                            }

                            is GameViewState.GameOver -> {
                                Log.d("What VM STATE 6", vm.screenState.toString())
                                GameOverScreen(
                                    vm = vm,
                                    onContinue = {
                                        navigate<LobbyListActivity> { intent ->
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        }
                                    },
                                )
                            }

                            is GameViewState.WaitingForAnte -> {
                                WaitingForAnteScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    vm = vm,
                                    onAntePlace = { ante ->
                                        Log.d("LobbyActivity", "Ante placed: $ante")
                                        vm.placeBet(ante)
                                    }
                                )
                            }

                            is GameViewState.RoundComplete -> {
                                Log.d("What VM STATE 5", vm.screenState.toString())
                                RoundCompleteScreen(
                                    vm = vm,
                                    onNextRound = {
                                        vm.nextRound()
                                    }
                                )
                            }

                            is GameViewState.TurnTimedOut -> {
                                Log.d("What VM STATE 4", vm.screenState.toString())
                                TurnTimedOutScreen(
                                    onContinue = {
                                        vm.getCurrentGameState()
                                    }
                                )
                            }

                            else -> {
                                Box {
                                    Text("Teste", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Box {
                            Text("Loading user...", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}