package pt.isel.daw.dicepoker.domain.players

import pt.isel.daw.dicepoker.domain.hand.Hand

data class PlayerSummary(
    val userId: Int,
    val balance: Int,
    val currentHand: Hand? = null,
    val hasPlayed: Boolean = false,
    val isHost: Boolean = false,
)
