package com.example.chelaspokerdice.domain.dto
import com.example.chelaspokerdice.domain.Dice
import kotlinx.serialization.Serializable

@Serializable
data class DiceDTO(
    val face: String,
    val points: Int,
)

fun DiceDTO.toDice(): Dice {
    return Dice(
        face = this.face,
        points = this.points
    )
}
