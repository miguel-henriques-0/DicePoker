package com.example.chelaspokerdice.views.createLobbyActivity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chelaspokerdice.R
import com.example.chelaspokerdice.domain.Game
import com.example.chelaspokerdice.ui.theme.ChelasPokerDiceTheme
import com.example.chelaspokerdice.views.components.LoadingComponent
import com.example.chelaspokerdice.views.createLobbyActivity.components.CreateLobby

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLobbyScreen(
    viewModel: CreateLobbyViewModel,
    modifier: Modifier,
    onCreateLobby: (Game) -> Unit,
    onNavigateBack: () -> Unit = {}
){
    val user = viewModel.player.collectAsState().value

    ChelasPokerDiceTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.create_lobby),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = R.string.back.toString(),
                                tint = Color.White
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
                    .background(Color(0xFF1A1A2E))
                    .padding(innerPadding)
            ) {
                when (val state = viewModel.screenState) {
                    is CreateLobbyViewModel.CreateLobbyState.Success -> {
                        onCreateLobby(state.game)
                    }

                    is CreateLobbyViewModel.CreateLobbyState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingComponent(modifier)
                        }
                    }

                    is CreateLobbyViewModel.CreateLobbyState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF16213E)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6B6B),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(R.string.error_occurred),
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.error_occurred),
                                        color = Color(0xFFB0B0C3),
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = {
                                            viewModel.screenState = CreateLobbyViewModel.CreateLobbyState.Idle
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF6C63FF)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.try_again),
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    is CreateLobbyViewModel.CreateLobbyState.Idle -> {
                        CreateLobby(
                            vm = viewModel,
                            modifier = Modifier,
                            onCreateLobby = {
                                viewModel.createLobby(
                                    name = viewModel.lobbyName,
                                    description = viewModel.description,
                                    maxPlayers = viewModel.selectedPlayer.toInt(),
                                    rounds = viewModel.selectedRounds.toInt(),
                                    player = user!!
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
