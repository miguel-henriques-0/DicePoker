package com.example.chelaspokerdice.views.lobbyActivity.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chelaspokerdice.domain.PlayAction
import com.example.chelaspokerdice.views.lobbyActivity.LobbyActivityViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import com.example.chelaspokerdice.R

data class GameAction(
    val action: PlayAction,
    val rollDices: List<Int> = emptyList(),
)

@Composable
fun GameScreen(
    modifier: Modifier,
    onPlay: (GameAction) -> Unit,
    vm: LobbyActivityViewModel,
) {
    var selectedDice by rememberSaveable { mutableStateOf(setOf<Int>()) }

    val isCurrentPlayer = vm.game?.gameState?.currentPlayerId == vm.user.collectAsState().value?.id
    val canRoll = (vm.game?.gameState?.rollsForTurn ?: 0) > 0 && selectedDice.isNotEmpty() && isCurrentPlayer

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213E)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            ScorePanel(vm = vm)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213E)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            DiceDisplay(
                selectedIndices = selectedDice,
                onDiceClick = { index ->
                    if (isCurrentPlayer) {
                        selectedDice = if (selectedDice.contains(index)) {
                            selectedDice - index
                        } else {
                            selectedDice + index
                        }
                    }
                },
                vm = vm
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213E)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF6C63FF).copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.rolls_left),
                            color = Color(0xFFB0B0B0),
                            fontSize = 12.sp
                        )
                        vm.game?.gameState?.rollsForTurn?.let {
                            Text(
                                text = "$it",
                                color = Color(0xFF6C63FF),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = {
                            onPlay(GameAction(action = PlayAction.END))
                        },
                        enabled = isCurrentPlayer,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF),
                            disabledContainerColor = Color(0xFF3A3A5C).copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.end_turn),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Button(
                        onClick = {
                            onPlay(
                                GameAction(
                                    action = PlayAction.ROLL,
                                    rollDices = selectedDice.toList()
                                )
                            )
                        },
                        enabled = canRoll,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF),
                            disabledContainerColor = Color(0xFF3A3A5C).copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.roll_dice),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}