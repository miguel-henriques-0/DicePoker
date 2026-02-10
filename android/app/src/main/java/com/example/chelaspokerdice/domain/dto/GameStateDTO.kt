package com.example.chelaspokerdice.domain.dto
import com.example.chelaspokerdice.domain.GameState

import kotlinx.serialization.Serializable

@Serializable
data class GameStateDTO(
    val gameId: Int,
    val currentPlayerId: Int,
    val currentRound: Int,
    val maxRounds: Int,
    val playerList: List<PlayerSummaryDTO>,
    val playerHands: Map<Int, HandDTO>? = null,
    val playersExcluded: List<Int>? = null,
    val ante: Int? = null,
    val playersPaidAnte: Map<Int, Int>? = null,
    val pot: Int? = null,
    val roundWinner: List<PlayerSummaryDTO>? = null,
    val winners: List<PlayerSummaryDTO>? = null,
    val turnTimer: Long? = null,
    val turnStarted: Long? = null,
    val rollsForTurn: Int? = null,
)

fun GameStateDTO.toGameState(): GameState {
    return GameState(
        gameId = this.gameId,
        currentPlayerId = this.currentPlayerId,
        currentRound = this.currentRound,
        maxRounds = this.maxRounds,
        playerList = this.playerList.map { it.toPlayerSummary() },
        playerHands = this.playerHands?.mapValues { it.value.toHand() } ?: emptyMap(),
        playersExcluded = this.playersExcluded ?: emptyList(),
        ante = this.ante ?: 0,
        playersPaidAnte = this.playersPaidAnte ?: emptyMap(),
        pot = this.pot ?: 0,
        roundWinner = this.roundWinner?.map { it.toPlayerSummary() } ?: emptyList(),
        winners = this.winners?.map { it.toPlayerSummary() } ?: emptyList(),
        turnTimer = this.turnTimer ?: 0,
        turnStarted = this.turnStarted ?: 0,
        rollsForTurn = this.rollsForTurn ?: 0
    )
}