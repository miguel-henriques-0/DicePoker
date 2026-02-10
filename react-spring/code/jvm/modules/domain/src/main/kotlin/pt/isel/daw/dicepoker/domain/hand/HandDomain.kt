package pt.isel.daw.dicepoker.domain.hand
import org.springframework.stereotype.Component
import pt.isel.daw.dicepoker.domain.dice.Dice
import pt.isel.daw.dicepoker.domain.dice.DiceFace

@Component
class HandDomain {
    fun createHand(listDice: List<Dice>): Hand {
        return Hand(
            dices = listDice,
            handRank = getHandRank(listDice),
            points = getPoints(listDice, getHandRank(listDice)),
        )
    }

    fun getHandRank(dices: List<Dice>): HandRank {
        // Bust -> No sequence or pairs
        // Pairs -> Check for 1/2 or 3 pais
        // Straight -> Check for sequence in all 5 dices
        // Full House -> Check for a triple and a double where values are not the same
        // Four of a Kind -> 4 dices are the same
        // Five of a kind -> All dices match

        val handRankMap = dices.groupingBy { it.face }.eachCount()
        // Five of a kind
        if (handRankMap.any { it.value == 5 }) return HandRank.FIVE_OF_KIND
        // Four of a kind
        if (handRankMap.any { it.value == 4 }) return HandRank.FOUR_OF_KIND
        // Full house
        if (handRankMap.any { it.value == 3 } && handRankMap.any { it.value == 2 }) return HandRank.FULL_HOUSE
        // Three of a kind
        if (handRankMap.any { it.value == 3 }) return HandRank.THREE_OF_KIND
        // Two pairs
        if (handRankMap.count { it.value == 2 } == 2) return HandRank.TWO_PAIRS
        // One pair
        if (handRankMap.any { it.value == 2 }) return HandRank.ONE_PAIR
        // Straight
        val uniqueFaces = dices.map { it.face }.distinct().sortedBy { it.ordinal }
        // We check for 4 since a straight is dices from either 9 to King or 10 to Ace
        // Which means the points value from on to another is 4
        if (uniqueFaces.size == 5 && uniqueFaces.last().ordinal - uniqueFaces.first().ordinal == 4) {
            return HandRank.STRAIGHT
        }
        // Bust
        return HandRank.BUST
    }

    fun getPoints(
        dices: List<Dice>,
        handRank: HandRank,
    ): Int {
        val dicesGroupByFace = dices.groupingBy { it.face }.eachCount()

        return when (handRank) {
            HandRank.FOUR_OF_KIND -> {
                return sumPoints(dicesGroupByFace)
            }

            HandRank.THREE_OF_KIND -> {
                return sumPoints(dicesGroupByFace)
            }

            HandRank.TWO_PAIRS -> {
                return sumPoints(dicesGroupByFace)
            }

            HandRank.ONE_PAIR -> {
                return sumPoints(dicesGroupByFace)
            }

            HandRank.BUST -> 0

            else -> {
                dices.sumOf { it.points }
            }
        }
    }

    fun sumPoints(diceMap: Map<DiceFace, Int>): Int {
        var pointsSum = 0
        diceMap.forEach {
            if (it.value > 1) {
                // points = currPoins + face.points * numberOfOccurrences
                pointsSum += it.key.points * it.value
            }
        }
        return pointsSum
    }
}
