package pt.isel.daw.dicepoker.domain.hand

import pt.isel.daw.dicepoker.domain.dice.Dice

data class Hand(
    val dices: List<Dice>,
    val handRank: HandRank? = null,
    val points: Int = 0,
) {
    companion object {
        fun createInitialHand(handSize: Int): Hand =
            Hand(
                Dice.createSetOfDices(handSize),
            )
    }
}
