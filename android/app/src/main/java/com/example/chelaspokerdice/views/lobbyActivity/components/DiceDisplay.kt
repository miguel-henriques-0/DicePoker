package com.example.chelaspokerdice.views.lobbyActivity.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.chelaspokerdice.views.lobbyActivity.LobbyActivityViewModel

@Composable
fun DiceDisplay(
    selectedIndices: Set<Int>,
    onDiceClick: (Int) -> Unit,
    vm: LobbyActivityViewModel
) {
    val game = vm.game
    Log.d("DiceDisplay", "Game state: $game")
    Log.d("DiceDisplay", "Current Player ID: ${vm.game?.gameState?.currentPlayerId}")
    Log.d("DiceDisplay", "Player Hands: ${game?.gameState?.playerHands}")
    val hand = game?.gameState?.playerHands?.get(vm.game?.gameState?.currentPlayerId)
    Log.d("DiceDisplay", "Current Player Hand: $hand")

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF399059)),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        hand?.dices?.forEachIndexed { index, dice ->
            Dice(
                face = dice.face,
                isSelected = selectedIndices.contains(index),
                onClick = { onDiceClick(index) }
            )
        }
    }
}
