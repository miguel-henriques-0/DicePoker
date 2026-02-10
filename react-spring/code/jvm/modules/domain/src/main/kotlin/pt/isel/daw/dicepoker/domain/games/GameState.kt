package pt.isel.daw.dicepoker.domain.games
import pt.isel.daw.dicepoker.domain.hand.Hand
import pt.isel.daw.dicepoker.domain.players.PlayerSummary
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


/*
    Sealed Interface to define all possible GameState's

    gameId -> The game ID
    currentPlayerId -> The ID of the current turn player
    currentRound -> The current round being played
    maxRounds -> The game total rounds to be played
    playerList -> A list containing objects <PLayerSummary> with info about the players balance, etc.
    playerHands -> A map of userId to Hand, that contains all hands for the players ingame
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = InLobby::class, name = "InLobby"),
    JsonSubTypes.Type(value = WaitingForAnte::class, name = "WaitingForAnte"),
    JsonSubTypes.Type(value = WaitingForPlayerAction::class, name = "WaitingForPlayerAction"),
    JsonSubTypes.Type(value = RoundComplete::class, name = "RoundComplete"),
    JsonSubTypes.Type(value = GameOver::class, name = "GameOver"),
    JsonSubTypes.Type(value = TurnTimedOut::class, name = "TurnTimedOut"),
)
sealed interface GameState {
    val gameId: Int
    val currentPlayerId: Int
    val currentRound: Int
    val maxRounds: Int
    val playerList: List<PlayerSummary>
    val playerHands: Map<Int, Hand>
    val playersExcluded: List<Int>

    fun canRoll(userId: Int): Boolean

    fun canEndTurn(userId: Int): Boolean

    fun gameOver(): Boolean

    fun isInGame(userId: Int): Boolean
}

data class WaitingForAnte(
    override val gameId: Int,
    override val currentPlayerId: Int,
    override val currentRound: Int,
    override val maxRounds: Int,
    override val playerList: List<PlayerSummary>,
    override val playerHands: Map<Int, Hand> = emptyMap(),
    override val playersExcluded: List<Int>,
    val ante: Int,
    val playersPaidAnte: Map<Int, Int>,
) : GameState {
    override fun canRoll(userId: Int): Boolean = false

    override fun canEndTurn(userId: Int): Boolean = false

    override fun gameOver(): Boolean = playersExcluded.size >= playerList.size

    override fun isInGame(userId: Int): Boolean = playerList.find { userId == it.userId } != null

    fun canBet(userId: Int): Boolean = !playersPaidAnte.containsKey(userId)

    fun enoughBalanceAvailable(
        userId: Int,
        ante: Int,
    ): Boolean = playerList.find { userId == it.userId && it.balance >= ante } != null
}

data class RoundComplete(
    override val gameId: Int,
    override val currentPlayerId: Int,
    override val currentRound: Int,
    override val maxRounds: Int,
    override val playerList: List<PlayerSummary>,
    override val playerHands: Map<Int, Hand>,
    override val playersExcluded: List<Int>,
    val pot: Int,
    val roundWinner: List<PlayerSummary>,
) : GameState {
    override fun canRoll(userId: Int): Boolean = false

    override fun canEndTurn(userId: Int): Boolean = false

    override fun gameOver(): Boolean = true

    override fun isInGame(userId: Int): Boolean = playerList.find { userId == it.userId } != null
}

data class GameOver(
    override val gameId: Int,
    override val currentPlayerId: Int,
    override val currentRound: Int,
    override val maxRounds: Int,
    override val playerList: List<PlayerSummary>,
    override val playerHands: Map<Int, Hand>,
    override val playersExcluded: List<Int>,
    val winners: List<PlayerSummary>,
) : GameState {
    override fun canRoll(userId: Int): Boolean = false

    override fun canEndTurn(userId: Int): Boolean = false

    override fun gameOver(): Boolean = true

    override fun isInGame(userId: Int): Boolean = playerList.find { userId == it.userId } != null
}

data class TurnTimedOut(
    override val gameId: Int,
    override val currentPlayerId: Int,
    override val currentRound: Int,
    override val maxRounds: Int,
    override val playerList: List<PlayerSummary>,
    override val playerHands: Map<Int, Hand>,
    override val playersExcluded: List<Int>,
    val turnTimer: Long,
    val pot: Int,
) : GameState {
    override fun canRoll(userId: Int): Boolean = false

    override fun canEndTurn(userId: Int): Boolean = false

    override fun gameOver(): Boolean = false

    override fun isInGame(userId: Int): Boolean = playerList.find { userId == it.userId } != null
}

data class WaitingForPlayerAction(
    override val gameId: Int,
    override val currentPlayerId: Int,
    override val currentRound: Int,
    override val maxRounds: Int,
    override val playerList: List<PlayerSummary>,
    override val playerHands: Map<Int, Hand>,
    override val playersExcluded: List<Int>,
    val pot: Int,
    val turnStarted: Long,
    val turnTimer: Long,
    val rollsForTurn: Int,
) : GameState {
    override fun canRoll(userId: Int): Boolean = userId == currentPlayerId && rollsForTurn > 0

    override fun canEndTurn(userId: Int): Boolean = currentPlayerId == userId

    override fun gameOver(): Boolean = false

    override fun isInGame(userId: Int): Boolean = playerList.find { userId == it.userId } != null

    fun isTurn(userId: Int): Boolean = currentPlayerId == userId

    fun handHasDices(
        listOfDices: List<Int>,
        userId: Int,
    ): Boolean {
        if (playerHands.containsKey(userId)) {
            val heldDices = playerHands.getValue(userId).dices
            return listOfDices.all { it in heldDices.indices }
        }
        return false
    }

    fun getCurrentPlayerHand(userId: Int): Hand = playerHands.getValue(userId)
}

data class InLobby(
    override val gameId: Int = 0,
    override val currentPlayerId: Int = 0,
    override val currentRound: Int = 0,
    override val maxRounds: Int = 0,
    override val playerList: List<PlayerSummary>,
    override val playerHands: Map<Int, Hand> = emptyMap(),
    override val playersExcluded: List<Int> = emptyList(),
) : GameState {
    override fun canRoll(userId: Int): Boolean = false

    override fun canEndTurn(userId: Int): Boolean = false

    override fun gameOver(): Boolean = false

    override fun isInGame(userId: Int): Boolean = false
}
