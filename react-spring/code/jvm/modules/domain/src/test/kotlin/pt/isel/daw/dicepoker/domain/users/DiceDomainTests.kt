package pt.isel.daw.dicepoker.domain.users
import org.junit.jupiter.api.Assertions.assertTrue
import pt.isel.daw.dicepoker.domain.dice.Dice
import pt.isel.daw.dicepoker.domain.dice.DiceDomain
import pt.isel.daw.dicepoker.domain.dice.DiceFace
import kotlin.test.Test

class DiceDomainTests {
    @Test
    fun `Test dice roll`() {
        val dice =
            Dice(
                face = DiceFace.ACE,
                points = DiceFace.ACE.points,
            )

        val diceDomain = DiceDomain()

        val rolledDice = diceDomain.rollDice()

        assertTrue { rolledDice.face in DiceFace.entries }
        assertTrue { rolledDice.points == DiceFace.valueOf(rolledDice.face.toString()).points }
    }
}
