package pt.isel.daw.dicepoker.domain.games

import kotlinx.datetime.Clock
import org.springframework.stereotype.Component
import pt.isel.daw.dicepoker.domain.hand.Hand
import pt.isel.daw.dicepoker.domain.players.PlayerSummary
import kotlin.compareTo

@Component
class GameStateFactory(
    private val clock: Clock,
    private val config: GameDomainConfig,
) {
    fun createLobbyState(userId: Int): GameState {
        val host = PlayerSummary(userId = userId, balance = 20, isHost = true)
        val players = listOf(host)

        return InLobby(playerList = players)
    }

    fun createInitialGameState(
        gameId: Int,
        players: List<PlayerSummary>,
        startingPlayerId: Int,
        maxRounds: Int,
        ante: Int,
    ): GameState =
        WaitingForAnte(
            gameId = gameId,
            currentPlayerId = startingPlayerId,
            currentRound = 1,
            maxRounds = maxRounds,
            ante = ante,
            playersPaidAnte = emptyMap(),
            playersExcluded = emptyList(),
            playerList = players,
        )

    fun nextGameState(
        currentGameState: GameState,
        action: GameAction,
    ): GameState {
        return when (currentGameState) {
            is GameOver -> currentGameState
            is RoundComplete -> evaluateIsNextRound(currentGameState)
            is TurnTimedOut -> evaluateTimeoutNextState(currentGameState)
            is WaitingForAnte -> evaluateAnteState(currentGameState, action)
            is WaitingForPlayerAction -> evaluatePlayerAction(currentGameState, action)
            else -> currentGameState
        }
    }

    fun addPlayer(
        currentGameState: GameState,
        player: PlayerSummary,
    ): GameState {
        val gameState = when (currentGameState) {
            is InLobby -> currentGameState.copy(
                playerList = currentGameState.playerList + player,
            )

            else -> currentGameState
        }

        return gameState
    }

    private fun evaluateAnteState(
        currentGameState: WaitingForAnte,
        action: GameAction,
    ): GameState {
        return when (action) {
            is GameAction.PayAnte -> {
                val paymentsMap = currentGameState.playersPaidAnte.toMutableMap()
                val handsMap = currentGameState.playerHands.toMutableMap()
                val excludedPlayers = currentGameState.playersExcluded.toMutableList()
                val updatedPlayerInfoList = currentGameState.playerList.toMutableList()

                val bettingPlayer = currentGameState.playerList.find { player -> player.userId == action.userId }
                // TODO(This if is unnecessary we already check if a player can bet on the service REFACTOR!)
                if (action.ante > bettingPlayer!!.balance) {
                    excludedPlayers.add(bettingPlayer.userId)
                } else {
                    paymentsMap[bettingPlayer.userId] = action.ante
                    val updatedPlayer = decreaseBalance(bettingPlayer, action.ante)
                    val idx = updatedPlayerInfoList.indexOfFirst { it.userId == action.userId }
                    updatedPlayerInfoList[idx] = updatedPlayer
                    handsMap[action.userId] = action.startingHand
                }

                if (paymentsMap.size == currentGameState.playerList.size - excludedPlayers.size) {
                    // Reset player has played flag
                    val updatedPlayerList =
                        updatedPlayerInfoList.map { player ->
                            player.copy(
                                hasPlayed = false,
                            )
                        }
                    WaitingForPlayerAction(
                        gameId = currentGameState.gameId,
                        currentPlayerId = currentGameState.currentPlayerId,
                        currentRound = currentGameState.currentRound,
                        maxRounds = currentGameState.maxRounds,
                        playerList = updatedPlayerList,
                        turnStarted = clock.now().epochSeconds,
                        turnTimer = config.turnTimer,
                        rollsForTurn = config.maxRollsPerTurn,
                        playerHands = handsMap,
                        pot = currentGameState.ante,
                        playersExcluded = currentGameState.playersExcluded,
                    )
                } else {
                    currentGameState.copy(
                        playerList = updatedPlayerInfoList,
                        playersPaidAnte = paymentsMap,
                        playersExcluded = excludedPlayers,
                        playerHands = handsMap,
                        ante = currentGameState.ante + action.ante,
                    )
                }
            }
            else -> {
                currentGameState
            }
        }
    }

    private fun evaluatePlayerAction(
        currentGameState: WaitingForPlayerAction,
        action: GameAction,
    ): GameState {
        return when (action) {
            is GameAction.EndTurn -> {
                val hasTimedOut =
                    clock.now().epochSeconds >
                        currentGameState.turnStarted + currentGameState.turnTimer
                val updatedPlayerList = updatePlayerInfo(currentGameState.playerList, action.userId)

                if (hasTimedOut) {
                    val next = nextPlayerId(currentGameState, currentGameState.playersExcluded)
                    TurnTimedOut(
                        gameId = currentGameState.gameId,
                        currentPlayerId = next,
                        currentRound = currentGameState.currentRound,
                        maxRounds = currentGameState.maxRounds,
                        turnTimer = config.turnTimer,
                        playerList = updatedPlayerList,
                        playerHands = currentGameState.playerHands,
                        pot = currentGameState.pot,
                        playersExcluded = currentGameState.playersExcluded,
                    )
                } else {
                    if (nextRound(updatedPlayerList)) {
                        val winnersList =
                            calculateRoundWinner(
                                playersHand = currentGameState.playerHands,
                                playerList = currentGameState.playerList,
                                currentGameState.pot,
                            )
                        // Update winning player balance
                        val updatedPlayerListWithWinners =
                            updatedPlayerList.map { player ->
                                winnersList.find { it.userId == player.userId } ?: player
                            }

                        RoundComplete(
                            gameId = currentGameState.gameId,
                            currentPlayerId = currentGameState.currentPlayerId,
                            currentRound = currentGameState.currentRound,
                            maxRounds = currentGameState.maxRounds,
                            pot = currentGameState.pot,
                            playerHands = currentGameState.playerHands,
                            roundWinner = winnersList,
                            playerList = updatedPlayerListWithWinners,
                            playersExcluded = currentGameState.playersExcluded,
                        )
                    } else {
                        val next = nextPlayerId(currentGameState, currentGameState.playersExcluded)
                        WaitingForPlayerAction(
                            gameId = currentGameState.gameId,
                            currentPlayerId = next,
                            currentRound = currentGameState.currentRound,
                            maxRounds = currentGameState.maxRounds,
                            playerList = updatedPlayerList,
                            turnTimer = config.turnTimer,
                            rollsForTurn = config.maxRollsPerTurn,
                            turnStarted = clock.now().epochSeconds,
                            playerHands = currentGameState.playerHands,
                            pot = currentGameState.pot,
                            playersExcluded = currentGameState.playersExcluded,
                        )
                    }
                }
            }
            is GameAction.Roll -> {
                val hasTimedOut =
                    clock.now().epochSeconds >
                        currentGameState.turnStarted + currentGameState.turnTimer
                // if time out
                if (hasTimedOut) {
                    val updatedPlayerList = updatePlayerInfo(currentGameState.playerList, action.userId)
                    TurnTimedOut(
                        gameId = currentGameState.gameId,
                        currentPlayerId = nextPlayerId(currentGameState, currentGameState.playersExcluded),
                        currentRound = currentGameState.currentRound,
                        maxRounds = currentGameState.maxRounds,
                        turnTimer = config.turnTimer,
                        playerList = updatedPlayerList,
                        playerHands = currentGameState.playerHands,
                        pot = currentGameState.pot,
                        playersExcluded = currentGameState.playersExcluded,
                    )
                } else {
                    // Turn didnt timeout
                    val updateHandMap = currentGameState.playerHands.toMutableMap()
                    updateHandMap.forEach {
                        if (it.key == currentGameState.currentPlayerId) {
                            updateHandMap[it.key] = action.hand
                        }
                    }
                    // No more rolls end turn
                    if (currentGameState.rollsForTurn - 1 == 0) {
                        val updatedPlayerList = updatePlayerInfo(currentGameState.playerList, action.userId)
                        // turns Finished advance round
                        if (nextRound(updatedPlayerList)) {
                            val winnersList =
                                calculateRoundWinner(
                                    playersHand = currentGameState.playerHands,
                                    playerList = currentGameState.playerList,
                                    currentGameState.pot,
                                )
                            val updatedPlayerListWithWinners =
                                updatedPlayerList.map { player ->
                                    winnersList.find { it.userId == player.userId } ?: player
                                }
                            RoundComplete(
                                gameId = currentGameState.gameId,
                                currentPlayerId = currentGameState.currentPlayerId,
                                currentRound = currentGameState.currentRound,
                                maxRounds = currentGameState.maxRounds,
                                pot = currentGameState.pot,
                                playerHands = updateHandMap,
                                roundWinner = winnersList,
                                playerList = updatedPlayerListWithWinners,
                                playersExcluded = currentGameState.playersExcluded,
                            )
                        } else {
                            // pass turn to next player
                            val next = nextPlayerId(currentGameState, currentGameState.playersExcluded)
                            val updatedPlayerList = updatePlayerInfo(currentGameState.playerList, action.userId)
                            WaitingForPlayerAction(
                                gameId = currentGameState.gameId,
                                currentPlayerId = next,
                                currentRound = currentGameState.currentRound,
                                maxRounds = currentGameState.maxRounds,
                                playerList = updatedPlayerList,
                                turnTimer = config.turnTimer,
                                rollsForTurn = config.maxRollsPerTurn,
                                turnStarted = clock.now().epochSeconds,
                                playerHands = updateHandMap,
                                pot = currentGameState.pot,
                                playersExcluded = currentGameState.playersExcluded,
                            )
                        }
                    } else {
                        // player turn not finished
                        WaitingForPlayerAction(
                            gameId = currentGameState.gameId,
                            currentPlayerId = currentGameState.currentPlayerId,
                            currentRound = currentGameState.currentRound,
                            maxRounds = currentGameState.maxRounds,
                            playerList = currentGameState.playerList,
                            turnTimer = config.turnTimer,
                            rollsForTurn = currentGameState.rollsForTurn - 1,
                            turnStarted = currentGameState.turnStarted,
                            playerHands = updateHandMap,
                            pot = currentGameState.pot,
                            playersExcluded = currentGameState.playersExcluded,
                        )
                    }
                }
            }
            else -> {
                currentGameState
            }
        }
    }

    private fun evaluateIsNextRound(currentGameState: RoundComplete): GameState {
        // Check if this was the final round
        if (gameEnded(currentGameState)) {
            val gameWinners = calculateGameWinners(
                playersList = currentGameState.playerList,
            )
            return GameOver(
                gameId = currentGameState.gameId,
                currentPlayerId = currentGameState.currentPlayerId,
                currentRound = currentGameState.currentRound, // Keep current round
                maxRounds = currentGameState.maxRounds,
                playerList = currentGameState.playerList,
                playerHands = currentGameState.playerHands,
                winners = gameWinners,
                playersExcluded = currentGameState.playersExcluded,
            )
        }

        val playersExcludedList = removePlayersWithInsufficientBalance(
            currentGameState.playerList,
            currentGameState.playersExcluded,
        )

        // Check if only one player remains with balance
        val playersWithBalance = currentGameState.playerList.count {
            it.balance > 0 && !playersExcludedList.contains(it.userId)
        }

        if (playersWithBalance <= 1) {
            val gameWinners = calculateGameWinners(
                playersList = currentGameState.playerList,
            )
            return GameOver(
                gameId = currentGameState.gameId,
                currentPlayerId = currentGameState.currentPlayerId,
                currentRound = currentGameState.currentRound,
                maxRounds = currentGameState.maxRounds,
                playerList = currentGameState.playerList,
                playerHands = currentGameState.playerHands,
                winners = gameWinners,
                playersExcluded = playersExcludedList,
            )
        }

        // Only create WaitingForAnte if we haven't exceeded max rounds
        return WaitingForAnte(
            gameId = currentGameState.gameId,
            currentPlayerId = nextPlayerId(currentGameState, playersExcludedList),
            currentRound = currentGameState.currentRound + 1,
            maxRounds = currentGameState.maxRounds,
            playerList = currentGameState.playerList,
            ante = 0,
            playersPaidAnte = emptyMap(),
            playersExcluded = playersExcludedList,
        )
    }
//        if (gameEnded(currentGameState)) {
//            val winnersList =
//                calculateRoundWinner(
//                    playersHand = currentGameState.playerHands,
//                    playerList = currentGameState.playerList,
//                    currentGameState.pot,
//                )
//            val gameWinners =
//                calculateGameWinners(
//                    playersList = winnersList,
//                )
//            return GameOver(
//                gameId = currentGameState.gameId,
//                currentPlayerId = currentGameState.currentPlayerId,
//                currentRound = currentGameState.currentRound,
//                maxRounds = currentGameState.maxRounds,
//                playerList = currentGameState.playerList,
//                playerHands = currentGameState.playerHands,
//                winners = gameWinners,
//                playersExcluded = currentGameState.playersExcluded,
//            )
//        } else {
//            val playersExcludedList =
//                removePlayersWithInsufficientBalance(
//                    currentGameState.playerList,
//                    currentGameState.playersExcluded,
//                )
//
//            return WaitingForAnte(
//                gameId = currentGameState.gameId,
//                currentPlayerId = nextPlayerId(currentGameState, playersExcludedList),
//                currentRound = currentGameState.currentRound + 1,
//                maxRounds = currentGameState.maxRounds,
//                playerList = currentGameState.playerList,
//                ante = 0,
//                playersPaidAnte = emptyMap(),
//                playersExcluded = playersExcludedList,
//            )
//        }
//    }

    private fun evaluateTimeoutNextState(currentGameState: TurnTimedOut): GameState {
        // Round Finished
        if (nextRound(currentGameState.playerList)) {
            val updatedPlayerList =
                updatePlayerInfo(
                    currentGameState.playerList,
                    currentGameState.currentPlayerId,
                )
            val winnersList =
                calculateRoundWinner(
                    playersHand = currentGameState.playerHands,
                    playerList = updatedPlayerList,
                    currentGameState.pot,
                )
            val updatedPlayerListWithWinners =
                updatedPlayerList.map { player ->
                    winnersList.find { it.userId == player.userId } ?: player
                }

            return RoundComplete(
                gameId = currentGameState.gameId,
                currentPlayerId = currentGameState.currentPlayerId,
                currentRound = currentGameState.currentRound,
                maxRounds = currentGameState.maxRounds,
                playerList = updatedPlayerListWithWinners,
                pot = currentGameState.pot,
                playerHands = emptyMap(),
                roundWinner = winnersList,
                playersExcluded = currentGameState.playersExcluded,
            )
        } else {
            // Same Round next player
            return WaitingForPlayerAction(
                gameId = currentGameState.gameId,
                currentPlayerId = currentGameState.currentPlayerId,
                currentRound = currentGameState.currentRound,
                maxRounds = currentGameState.maxRounds,
                playerList = currentGameState.playerList,
                turnTimer = config.turnTimer,
                rollsForTurn = config.maxRollsPerTurn,
                turnStarted = clock.now().epochSeconds,
                playerHands = currentGameState.playerHands,
                pot = currentGameState.pot,
                playersExcluded = currentGameState.playersExcluded,
            )
        }
    }

    private fun updateWinnersList(
        playerList: List<PlayerSummary>,
        pot: Int,
    ): List<PlayerSummary> {
        val potPerWinner = pot / playerList.size
        return playerList.map { player ->
            increaseBalance(player, potPerWinner)
        }
    }

    private fun updatePlayerInfo(
        playerList: List<PlayerSummary>,
        userId: Int,
    ): List<PlayerSummary> {
        val mutableListPlayer = playerList.toMutableList()
        val idx = playerList.indexOfFirst { it.userId == userId }
        val updatedPlayer = updateHasPlayed(mutableListPlayer[idx])
        mutableListPlayer[idx] = updatedPlayer
        return mutableListPlayer
    }

    private fun nextRound(playerList: List<PlayerSummary>): Boolean = playerList.all { it.hasPlayed }

    private fun nextPlayerId(
        currentGameState: GameState,
        playersExcluded: List<Int>,
    ): Int {
        var nextPlayerIdx = currentGameState.currentRound % currentGameState.playerList.size
        var nextPlayer = currentGameState.playerList[nextPlayerIdx]
        while (playersExcluded.contains(nextPlayer.userId)) {
            nextPlayerIdx = (nextPlayerIdx + 1) % playersExcluded.size
            nextPlayer = currentGameState.playerList[nextPlayerIdx]
        }
        return nextPlayer.userId
    }

    private fun gameEnded(currentGameState: GameState): Boolean = currentGameState.maxRounds == currentGameState.currentRound

    private fun decreaseBalance(
        bettingPlayer: PlayerSummary,
        ante: Int,
    ): PlayerSummary =
        bettingPlayer.copy(
            balance = bettingPlayer.balance - ante,
        )

    private fun increaseBalance(
        bettingPlayer: PlayerSummary,
        pot: Int,
    ): PlayerSummary =
        bettingPlayer.copy(
            balance = bettingPlayer.balance + pot,
        )

    private fun updateHasPlayed(playerSummary: PlayerSummary) =
        playerSummary.copy(
            hasPlayed = !playerSummary.hasPlayed,
        )

    private fun calculateRoundWinner(
        playersHand: Map<Int, Hand>,
        playerList: List<PlayerSummary>,
        pot: Int,
    ): List<PlayerSummary> {
        val maxPoints = playersHand.maxOf { it.value.points }
        val winnerList = mutableListOf<PlayerSummary>()

        playersHand.forEach { (key, value) ->
            if (value.points == maxPoints) {
                val winner = playerList.find { it.userId == key }
                if (winner != null) {
                    winnerList.add(winner)
                }
            }
        }
        return updateWinnersList(winnerList, pot)
    }

    private fun removePlayersWithInsufficientBalance(
        playerList: List<PlayerSummary>,
        excludedPlayers: List<Int>,
    ): List<Int> {
        val mutableExcludedPlayers = excludedPlayers.toMutableList()

        playerList.forEach { player ->
            if (player.balance == 0 && !excludedPlayers.contains(player.userId)) {
                mutableExcludedPlayers.add(player.userId)
            }
        }
        return mutableExcludedPlayers
    }

    private fun calculateGameWinners(playersList: List<PlayerSummary>): List<PlayerSummary> {
        val maxBalance = playersList.maxOf { it.balance }
        val winnerList = mutableListOf<PlayerSummary>()
        playersList.forEach { player ->
            if (player.balance == maxBalance) {
                winnerList.add(player)
            }
        }
        return winnerList
    }
}
