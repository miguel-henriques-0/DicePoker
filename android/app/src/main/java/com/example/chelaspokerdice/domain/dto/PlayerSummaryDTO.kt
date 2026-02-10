package com.example.chelaspokerdice.domain.dto
import com.example.chelaspokerdice.domain.PlayerSummary
import kotlinx.serialization.Serializable

@Serializable
data class PlayerSummaryDTO(
    val userId: Int,
    val balance: Int,
    val currentHand: HandDTO? = null,
    val hasPlayed: Boolean = false,
    val isHost: Boolean = false,
)

fun PlayerSummaryDTO.toPlayerSummary(): PlayerSummary {
    return PlayerSummary(
        userId = this.userId,
        balance = this.balance,
        currentHand = this.currentHand?.toHand(),
        hasPlayed = this.hasPlayed,
        isHost = this.isHost
    )
}