package pt.isel.daw.dicepoker.services

import jakarta.inject.Named
import org.slf4j.LoggerFactory
import pt.isel.daw.dicepoker.domain.dice.DiceDomain
import pt.isel.daw.dicepoker.domain.games.AdvanceRoundError
import pt.isel.daw.dicepoker.domain.games.AdvanceRoundResult
import pt.isel.daw.dicepoker.domain.games.Game
import pt.isel.daw.dicepoker.domain.games.GameAction
import pt.isel.daw.dicepoker.domain.games.GameCreationError
import pt.isel.daw.dicepoker.domain.games.GameCreationResult
import pt.isel.daw.dicepoker.domain.games.GameDomain
import pt.isel.daw.dicepoker.domain.games.GameGetError
import pt.isel.daw.dicepoker.domain.games.GameGetResult
import pt.isel.daw.dicepoker.domain.games.GameJoinError
import pt.isel.daw.dicepoker.domain.games.GameJoinResult
import pt.isel.daw.dicepoker.domain.games.GameLeaveError
import pt.isel.daw.dicepoker.domain.games.GameLeaveResult
import pt.isel.daw.dicepoker.domain.games.GameList
import pt.isel.daw.dicepoker.domain.games.GameListResult
import pt.isel.daw.dicepoker.domain.games.GameOver
import pt.isel.daw.dicepoker.domain.games.GameStartError
import pt.isel.daw.dicepoker.domain.games.GameStartResult
import pt.isel.daw.dicepoker.domain.games.GameState
import pt.isel.daw.dicepoker.domain.games.GameStateFactory
import pt.isel.daw.dicepoker.domain.games.GameStatus
import pt.isel.daw.dicepoker.domain.games.InLobby
import pt.isel.daw.dicepoker.domain.games.PlayError
import pt.isel.daw.dicepoker.domain.games.PlayResult
import pt.isel.daw.dicepoker.domain.games.RoundComplete
import pt.isel.daw.dicepoker.domain.games.TurnTimedOut
import pt.isel.daw.dicepoker.domain.games.WaitingForAnte
import pt.isel.daw.dicepoker.domain.games.WaitingForPlayerAction
import pt.isel.daw.dicepoker.domain.hand.HandDomain
import pt.isel.daw.dicepoker.domain.players.PlayerSummary
import pt.isel.daw.dicepoker.repository.TransactionManager
import pt.isel.daw.dicepoker.utils.failure
import pt.isel.daw.dicepoker.utils.success
import kotlin.run

@Named
class GameService(
    private val transactionManager: TransactionManager,
    private val gameStateFactory: GameStateFactory,
    private val gamesDomain: GameDomain,
    private val handDomain: HandDomain,
    private val diceDomain: DiceDomain,
) {
    fun listGames(
        order: String = "asc",
        limit: Int = 20,
        status: String = GameStatus.OPEN.name,
        lastGameId: Int?,
    ): GameListResult {
        return transactionManager.run {
            val gameList: List<Game> = it.gamesRepository.listGames(
                order = order,
                limit = limit,
                status = status,
                lastGameId = lastGameId
            )
            if (gameList.size < limit) {
                return@run success(GameList(gameList, null))
            } else {
                return@run success(GameList(gameList, gameList.last().id))
            }
        }
    }

    fun createGame(
        name: String,
        description: String,
        rounds: Int,
        maxPlayers: Int,
        minPlayers: Int,
        timeout: Long?,
        userId: Int,
        username: String,
    ): GameCreationResult {
        val listOfUserGames = getUserGames(userId)
        var startTimer = timeout
        if (startTimer == null) startTimer = gamesDomain.lobbyTimeout
        if (gamesDomain.checkMaxConcurrentGamesReached(listOfUserGames.size)) {
            val lobbyState = gameStateFactory.createLobbyState(userId)
            logger.info("createGame: lobby players = ${lobbyState.playerList.map { it.userId }}")
            return transactionManager.run {
                val gameRepo = it.gamesRepository
//                val lobbyState = gameStateFactory.createLobbyState(userId)
                val newGame =
                    gameRepo.createGame(
                        name = name,
                        description = description,
                        rounds = rounds,
                        maxPlayers = maxPlayers,
                        minPlayers = minPlayers,
                        timeout = startTimer,
                        userId = userId,
                        username = username,
                        gameState = lobbyState,
                    )
                success(newGame)
            }
        } else {
            return failure(GameCreationError.PlayerCantCreateMoreGames)
        }
    }

    fun joinGame(
        gameId: Int,
        userId: Int,
    ): GameJoinResult {
        val listOfUserGames = getUserGames(userId)
        if (gamesDomain.checkMaxConcurrentGamesReached(listOfUserGames.size)) {
            return transactionManager.run {
                val game = it.gamesRepository.getGameById(gameId)
                if (game == null) {
                    return@run failure(GameJoinError.GameDoesNotExist)
                } else {
                    val playersInGame = it.gamesRepository.getPlayersWaiting(gameId)
                    if (gamesDomain.isFull(game.maxPlayers, playersInGame.size)) {
                        return@run failure(GameJoinError.GameIsFull)
                    }
                    if (gamesDomain.isUserInGame(playersInGame, userId)) {
                        return@run failure(GameJoinError.PlayerAlreadyInGame)
                    }
                    if (gamesDomain.gameAlreadyInProgress(game)) return@run failure(GameJoinError.GameAlreadyInProgress)
                    it.gamesRepository.joinGame(
                        gameId,
                        userId,
                    )

                    val updatedGameState =
                        gameStateFactory.addPlayer(
                            game.gameState!!,
                            PlayerSummary(
                                userId = userId,
                                balance = 20,
                                isHost = false,
                            ),
                        )
                    logger.info("updatedGameState after join: {}", updatedGameState)
                    val joinedGame = it.gamesRepository.updateGame(gameId, gameStatus = GameStatus.OPEN, gameState = updatedGameState)
                    logger.info("joinGame: {}", joinedGame)
                    return@run success(joinedGame)
                }
            }
        }
        return failure(GameJoinError.PlayerCantJoinMoreGames)
    }

    fun getUserGames(userId: Int): List<Game> {
        return transactionManager.run {
            it.gamesRepository.getUserGame(
                userId = userId,
            )
        }
    }

    fun startGame(
        gameId: Int,
        userId: Int,
    ): GameStartResult {
        val game = getGame(gameId) ?: return failure(GameStartError.GameDoesNotExist)
        return transactionManager.run {
            val playersInGame = it.gamesRepository.getPlayersWaiting(gameId)
            if (!gamesDomain.isUserInGame(playersInGame, userId)) return@run failure(GameStartError.UserNotInGame)
            if (!gamesDomain.isGameHost(playersInGame, userId)) return@run failure(GameStartError.UserIsNotHost)

            if (!gamesDomain.canStartGame(
                    maxPlayers = game.maxPlayers,
                    minPlayers = game.minPlayers,
                    currentPlayers = playersInGame.size,
                    createdAt = game.createdAt,
                )
            ) {
                return@run failure(GameStartError.GameStartRequirementsNotFulfilled)
            }

            val playerSummaryList = gamesDomain.createPlayerSummary(playersInGame)

            val initialGameState =
                gameStateFactory.createInitialGameState(
                    gameId = gameId,
                    players = playerSummaryList,
                    startingPlayerId = playerSummaryList.first().userId,
                    maxRounds = game.rounds,
                    ante = 0,
                )

            val updatedGame =
                it.gamesRepository.updateGame(
                    gameId = gameId,
                    gameStatus = GameStatus.PLAYING,
                    gameState = initialGameState,
                )
            return@run success(updatedGame)
        }
    }

    fun getGameById(gameId: Int): GameGetResult {
        val game = getGame(gameId)
        if (game != null) return success(game)
        return failure(GameGetError.GameDoesNotExist)
    }

    fun getGame(gameId: Int): Game? {
        return transactionManager.run {
            it.gamesRepository.getGameById(gameId)
        }
    }

    private fun computeNextState(game: Game): GameState {
        return when (game.gameState) {
            is TurnTimedOut -> {
                val endState = game.gameState as TurnTimedOut
                gameStateFactory.nextGameState(endState, GameAction.Empty)
            }
            is RoundComplete -> {
                val roundCompleteState = game.gameState as RoundComplete
                gameStateFactory.nextGameState(roundCompleteState, GameAction.Empty)
            }
            else -> game.gameState!!
        }
    }

    fun leaveGame(
        gameId: Int,
        userId: Int,
    ): GameLeaveResult {
        return transactionManager.run {
            val game = it.gamesRepository.getGameById(gameId)
                ?: return@run failure(GameLeaveError.GameDoesNotExist)

            val playersInGame = it.gamesRepository.getPlayersWaiting(gameId)

            if (!gamesDomain.isUserInGame(playersInGame, userId)) {
                return@run failure(GameLeaveError.UserNotInGame)
            }

            if (!gamesDomain.canLeaveGame(game)) {
                return@run failure(GameLeaveError.GameStatusClosedOrFinished)
            }

            when (game.gameState) {
                is InLobby -> {
                    if (gamesDomain.isGameHost(playersInGame, userId)) {
                        it.gamesRepository.leaveHostGame(gameId, userId)

                        val updatedGame = it.gamesRepository.updateGameStatus(
                            gameId = gameId,
                            status = GameStatus.CLOSED,
                        )
                        return@run success(updatedGame)
                    } else {
                        it.gamesRepository.leaveGame(gameId, userId)

                        val gameState = game.gameState as InLobby
                        val updatedPlayers = gameState.playerList.filter { it.userId != userId } ?: emptyList()
                        val updatedGameState = gameState.copy(playerList = updatedPlayers)

                        val updatedGame = it.gamesRepository.updateGame(
                            gameId = gameId,
                            gameStatus = GameStatus.OPEN,
                            gameState = updatedGameState
                        )
                        return@run success(updatedGame)
                    }
                }

                is GameOver -> {
                    val gameOverState = game.gameState as GameOver

                    playersInGame.forEach { player ->
                        it.gamesRepository.leaveGame(gameId, player.userId)
                    }

                    val updatedGame = it.gamesRepository.updateGame(
                        gameId = gameId,
                        gameStatus = GameStatus.FINISHED,
                        gameState = gameOverState
                    )
                    return@run success(updatedGame)
                }

                else -> {
                    val updatedGameState = when (val currentState = game.gameState) {
                        is WaitingForAnte -> {
                            val updatedPlayers = currentState.playerList.filter { it.userId != userId }
                            val updatedExcluded = currentState.playersExcluded + userId
                            currentState.copy(
                                playerList = updatedPlayers,
                                playersExcluded = updatedExcluded
                            )
                        }
                        is WaitingForPlayerAction -> {
                            val updatedPlayers = currentState.playerList.filter { it.userId != userId }
                            val updatedExcluded = currentState.playersExcluded + userId
                            currentState.copy(
                                playerList = updatedPlayers,
                                playersExcluded = updatedExcluded
                            )
                        }
                        else -> currentState
                    }

                    it.gamesRepository.leaveGame(gameId, userId)

                    val updatedGame = it.gamesRepository.updateGame(
                        gameId = gameId,
                        gameStatus = GameStatus.valueOf(game.status),
                        gameState = updatedGameState!!,
                    )
                    return@run success(updatedGame)
                }
            }
        }
    }

    fun applyEndTurn(
        gameId: Int,
        userId: Int,
    ): PlayResult {
        return transactionManager.run {
            val game = it.gamesRepository.getGameById(gameId) ?: return@run failure(PlayError.GameDoesNotExist)

            when (game.gameState) {
                is WaitingForPlayerAction -> {
                    val endTurnState = game.gameState as WaitingForPlayerAction
                    if (!endTurnState.isInGame(userId)) return@run failure(PlayError.UserNotInGame)
                    if (!endTurnState.canEndTurn(userId)) return@run failure(PlayError.NotUserTurn)
                    val nextState =
                        gameStateFactory.nextGameState(
                            endTurnState,
                            GameAction.EndTurn(userId),
                        )

                    val updatedGame =
                        it.gamesRepository.updateGame(
                            gameId,
                            GameStatus.valueOf(game.status),
                            nextState,
                        )
                    return@run success(updatedGame)
                }
                is TurnTimedOut -> {
                    val timeOutState = game.gameState as TurnTimedOut
                    if (!timeOutState.isInGame(userId)) return@run failure(PlayError.UserNotInGame)
                    val updatedGameState =
                        gameStateFactory.nextGameState(
                            timeOutState,
                            GameAction.Empty,
                        )

                    val updatedGame =
                        it.gamesRepository.updateGame(
                            gameId,
                            GameStatus.valueOf(game.status),
                            updatedGameState,
                        )
                    return@run success(updatedGame)
                }
                else -> return@run failure(PlayError.InvalidAction)
            }
        }
    }

    fun applyBet(
        gameId: Int,
        userId: Int,
        ante: Int?,
    ): PlayResult {
        return transactionManager.run {
            val game = it.gamesRepository.getGameById(gameId) ?: return@run failure(PlayError.GameDoesNotExist)
            when (game.gameState) {
                is WaitingForAnte -> {
                    val anteState = game.gameState as WaitingForAnte

                    if (ante == null || ante <= 0) return@run failure(PlayError.InvalidBet)
                    if (!anteState.isInGame(userId)) return@run failure(PlayError.UserNotInGame)
                    if (!anteState.canBet(userId)) return@run failure(PlayError.AlreadyPlacedBet)
                    if (!anteState.enoughBalanceAvailable(userId, ante)) return@run failure(PlayError.InsufficientFunds)

                    val listOfDices = diceDomain.getRandomDiceList(gamesDomain.handSize)
                    val hand = handDomain.createHand(listOfDices)
                    val nextState =
                        gameStateFactory.nextGameState(
                            game.gameState!!,
                            action =
                                GameAction.PayAnte(
                                    userId = userId,
                                    ante = ante,
                                    startingHand = hand,
                                ),
                        )
                    val updatedGame =
                        it.gamesRepository.updateGame(
                            gameId = gameId,
                            gameState = nextState,
                            gameStatus = GameStatus.valueOf(game.status),
                        )
                    return@run success(updatedGame)
                }
                else -> return@run failure(PlayError.InvalidAction)
            }
        }
    }

    fun applyRoll(
        gameId: Int,
        userId: Int,
        listOfDices: List<Int>,
    ): PlayResult {
        return transactionManager.run {
            val game = it.gamesRepository.getGameById(gameId) ?: return@run failure(PlayError.GameDoesNotExist)

            when (game.gameState) {
                is WaitingForPlayerAction -> {
                    val actionState = game.gameState as WaitingForPlayerAction

                    if (!actionState.isInGame(userId)) return@run failure(PlayError.UserNotInGame)
                    if (!actionState.isTurn(userId)) return@run failure(PlayError.NotUserTurn)
                    if (!actionState.handHasDices(listOfDices, userId)) return@run failure(PlayError.InvalidDiceToRoll)

                    val currentHand = actionState.getCurrentPlayerHand(userId)
                    val mutableListDices = currentHand.dices.toMutableList()

                    mutableListDices.forEachIndexed { index, _ ->
                        if (listOfDices.contains(index)) {
                            mutableListDices[index] = diceDomain.rollDice()
                        }
                    }

                    val updatedHandRank = handDomain.getHandRank(mutableListDices)
                    val updatedHand =
                        currentHand.copy(
                            dices = mutableListDices,
                            handRank = updatedHandRank,
                            points = handDomain.getPoints(mutableListDices, updatedHandRank),
                        )

                    val updatedGameState =
                        gameStateFactory.nextGameState(
                            actionState,
                            action =
                                GameAction.Roll(
                                    userId = userId,
                                    hand = updatedHand,
                                ),
                        )

                    val updatedGame =
                        it.gamesRepository.updateGame(
                            gameId = gameId,
                            gameStatus = GameStatus.valueOf(game.status),
                            gameState = updatedGameState,
                        )
                    return@run success(updatedGame)
                }
                is TurnTimedOut -> {
                    val timeOutState = game.gameState as TurnTimedOut
                    if (!timeOutState.isInGame(userId)) return@run failure(PlayError.UserNotInGame)
                    val updatedGameState =
                        gameStateFactory.nextGameState(
                            timeOutState,
                            GameAction.Empty,
                        )
                    val updatedGame =
                        it.gamesRepository.updateGame(
                            gameId = gameId,
                            gameStatus = GameStatus.valueOf(game.status),
                            gameState = updatedGameState,
                        )
                    return@run success(updatedGame)
                }
                else -> return@run failure(PlayError.InvalidAction)
            }
        }
    }

    fun applyNextRound(
        gameId: Int,
        userId: Int,
    ): AdvanceRoundResult {
        return transactionManager.run {
            val game = it.gamesRepository.getGameById(gameId) ?: return@run failure(AdvanceRoundError.GameDoesNotExist)
            when (game.gameState) {
                is RoundComplete -> {
                    val roundCompleteState = game.gameState as RoundComplete
                    if (!roundCompleteState.isInGame(userId)) return@run failure(AdvanceRoundError.UserNotInGame)
                    val updatedGameState =
                        gameStateFactory.nextGameState(
                            roundCompleteState,
                            GameAction.Empty,
                        )

                    val updatedGame =
                        it.gamesRepository.updateGame(
                            gameId,
                            GameStatus.valueOf(game.status),
                            updatedGameState,
                        )
                    return@run success(updatedGame)
                }
                else -> return@run failure(AdvanceRoundError.InvalidAction)
            }
        }
    }

    fun updateGameStateOnTimeout(
        gameId: Int,
    ): PlayResult {
        return transactionManager.run {
            val game = it.gamesRepository.getGameById(gameId) ?: return@run failure(PlayError.GameDoesNotExist)

            val nextState = computeNextState(game)

            val updatedGame =
                it.gamesRepository.updateGame(
                    gameId,
                    GameStatus.valueOf(game.status),
                    nextState,
                )
            return@run success(updatedGame)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GameService::class.java)
    }
}
