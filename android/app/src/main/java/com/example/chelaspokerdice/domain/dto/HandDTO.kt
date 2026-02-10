package com.example.chelaspokerdice.domain.dto
import com.example.chelaspokerdice.domain.Hand
import kotlinx.serialization.Serializable

@Serializable
data class HandDTO(
    val dices: List<DiceDTO>,
    val handRank: String? = null,
    val points: Int = 0,
)

fun HandDTO.toHand(): Hand {
    return Hand(
        dices = this.dices.map { it.toDice() },
        handRank = this.handRank,
        points = this.points
    )
}