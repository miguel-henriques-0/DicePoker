package com.example.chelaspokerdice.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameState(
    val gameId: Int,
    val currentPlayerId: Int,
    val currentRound: Int,
    val maxRounds: Int,
    val playerList: List<PlayerSummary>, // List of players in the game
    val playerHands: Map<Int, Hand>, // Map with player id as key and Hand as value
    val playersExcluded: List<Int>, // List with id of player who have lost
    val ante: Int = 0, // Current ante value -> Used in WaitingForAnte state
    val playersPaidAnte: Map<Int, Int> = emptyMap(), // Map with player id as key and amount paid as value -> Used in WaitingForAnte state
    val pot: Int = 0, // Current pot value -> Used in RoundComplete, TurnTimedOut, WaitingForPlayerAction states
    val roundWinner: List<PlayerSummary> = emptyList(), // List of players who won the round -> Used in RoundComplete state
    val winners: List<PlayerSummary> = emptyList(), // List of players who won the game -> Used in GameOver state
    val turnTimer: Long = 0, // Remaining time for the current turn in seconds -> Used in TurnTimedOut, WaitingForPlayerAction states
    val turnStarted: Long = 0, // Timestamp in epoch seconds when the current turn started -> Used in WaitingForPlayerAction state
    val rollsForTurn: Int = 0, // Number of rolls made by the current player in this turn -> Used in WaitingForPlayerAction state
): Parcelable