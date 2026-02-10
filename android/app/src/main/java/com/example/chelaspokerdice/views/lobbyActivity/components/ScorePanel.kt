package com.example.chelaspokerdice.views.lobbyActivity.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.chelaspokerdice.R
import com.example.chelaspokerdice.views.lobbyActivity.LobbyActivityViewModel

@Composable
fun ScorePanel(
    vm: LobbyActivityViewModel
) {
    val game = vm.game
    val currentPlayerHand = game?.gameState?.playerHands?.get(game.gameState.currentPlayerId)
    Log.d("ScorePanel", "Current Player Hand: $currentPlayerHand")
    Log.d("ScorePanel", "Points: ${currentPlayerHand?.points}")
    Log.d("ScorePanel", "Hand Rank: ${currentPlayerHand?.handRank}")
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff1e2431)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.total_score), color = Color.White)
            Text("${currentPlayerHand?.points}", color = Color.Yellow)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.current_handrank), color = Color.White)
            Text(handRankToDisplay(currentPlayerHand?.handRank), color = Color.Yellow)
        }
    }
}

fun handRankToDisplay(handRank: String?): String {
    return when (handRank) {
        "BUST" -> "Bust!"
        "ONE_PAIR" -> "One Pair!"
        "TWO_PAIRS" -> "Two Pairs!"
        "THREE_OF_KIND" -> "Three of a Kind!"
        "STRAIGHT" -> "Straight!"
        "FULL_HOUSE" -> "Full House!"
        "FOUR_OF_KIND" -> "Four of a Kind!"
        "FIVE_OF_KIND" -> "Five of a Kind!"
        else -> "N/A"
    }
}