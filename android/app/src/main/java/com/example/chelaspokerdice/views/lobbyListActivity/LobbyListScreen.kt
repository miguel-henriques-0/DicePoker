package com.example.chelaspokerdice.views.lobbyListActivity

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chelaspokerdice.R
import com.example.chelaspokerdice.domain.Game
import com.example.chelaspokerdice.ui.theme.ChelasPokerDiceTheme
import com.example.chelaspokerdice.views.lobbyListActivity.components.LobbyList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyListScreen(
    viewModel: LobbyListViewModel,
    onCreateLobby: () -> Unit,
    onLobbySelection: (Game) -> Unit
) {
    val lobbyList by viewModel.games.collectAsState()

    ChelasPokerDiceTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color(0xFF1A1A2E),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.lobbies),
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        Button(
                            onClick = onCreateLobby,
                            modifier = Modifier.padding(end = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6C63FF)
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.create_lobby_button),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF16213E)
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFF1A1A2E))
            ) {
                LobbyList(
                    lobbyList = lobbyList,
                    lastId = viewModel.lastGameId.collectAsState().value,
                    onLobbySelect = { index ->
                        Log.d("LobbyListScreen", "Selected lobby at index: $index")
                        onLobbySelection(lobbyList[index])
                    },
                    onScrollEnd = {
                        viewModel.fetchLobbies()
                    },
                    onRefresh = {
                        viewModel.refreshLobbies()
                    }
                )
            }
        }
    }
}
