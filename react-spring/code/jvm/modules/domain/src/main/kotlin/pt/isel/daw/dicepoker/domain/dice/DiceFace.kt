package pt.isel.daw.dicepoker.domain.dice

enum class DiceFace(val points: Int) {
    ACE(6),
    KING(5),
    QUEEN(4),
    JACK(3),
    TEN(2),
    NINE(1),
}
