package com.example.chelaspokerdice.views.lobbyActivity.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chelaspokerdice.R
import com.example.chelaspokerdice.views.lobbyActivity.LobbyActivityViewModel

@Composable
fun RoundCompleteScreen(
    vm: LobbyActivityViewModel,
    onNextRound: () -> Unit
) {
    Log.e("RoundCompleteScreen", "Screen is being composed")
    val game = vm.game

    val currentRound = game?.gameState?.currentRound ?: 0
    val totalRounds = game?.rounds ?: 0
    Log.e("RoundCompleteScreen", "Current Round: $currentRound, " +
            "Total Rounds: $totalRounds")
    val isFinalRound = totalRounds == currentRound
    Log.e("IS FINAL ROUND", "$isFinalRound")

    val nextRoundText = if (isFinalRound) stringResource(R.string.continue_label) else stringResource(
        R.string.round
    ) + "${currentRound + 1}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213E)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.round_complete),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF6C63FF).copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.round) + "$currentRound of $totalRounds",
                        color = Color(0xFF6C63FF),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isFinalRound) {
                        stringResource(R.string.the_game_has_ended)
                    } else {
                        stringResource(R.string.get_ready_for_the_next_round)
                    },
                    color = Color(0xFFB0B0B0),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNextRound,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6C63FF)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = nextRoundText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
