package pt.isel.daw.dicepoker.domain.dice

import org.springframework.stereotype.Component

@Component
class DiceDomain {
    fun rollDice(): Dice {
        val newFace = DiceFace.entries.random()
        return Dice(
            newFace,
            newFace.points,
        )
    }

    fun getRandomDiceList(size: Int): List<Dice> {
        val diceList = mutableListOf<Dice>()
        repeat(size) {
            diceList.add(
                rollDice(),
            )
        }
        return diceList
    }
}
