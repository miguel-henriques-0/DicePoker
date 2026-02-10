package pt.isel.daw.dicepoker.domain.hand

enum class HandRank(val value: Int) {
    FIVE_OF_KIND(7),
    FOUR_OF_KIND(6),
    FULL_HOUSE(5),
    STRAIGHT(4),
    THREE_OF_KIND(3),
    TWO_PAIRS(2),
    ONE_PAIR(1),
    BUST(0),
}
