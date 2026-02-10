package pt.isel.daw.dicepoker.domain.games

import pt.isel.daw.dicepoker.domain.hand.Hand

sealed interface GameAction {
    data class PayAnte(val userId: Int, val ante: Int = 1, val startingHand: Hand) : GameAction

    data class Roll(val userId: Int, val hand: Hand) : GameAction

    data class EndTurn(val userId: Int) : GameAction
//    data class EndGame(val userId: Int): GameAction

    class Empty : GameAction {
        companion object EmptyAction : GameAction
    }
}
