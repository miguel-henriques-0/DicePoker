package pt.isel.daw.dicepoker.domain.dice

data class Dice(
    val face: DiceFace,
    val points: Int = face.points,
) {
    companion object {
        fun createInitialDice(): Dice {
            val initialFace = DiceFace.entries.random()
            return Dice(
                face = initialFace,
                points = initialFace.points,
            )
        }

        fun createSetOfDices(handSize: Int): List<Dice> {
            return List(handSize) { createInitialDice() }
        }
    }
}
