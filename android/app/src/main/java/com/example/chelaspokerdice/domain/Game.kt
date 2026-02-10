package com.example.chelaspokerdice.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Game (
    val id: Int,
    val name: String,
    val description: String,
    val rounds: Int,
    val status: String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val timeout: Long,
    val gameStateType: String,
    val gameState: GameState?
): Parcelable