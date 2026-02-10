package pt.isel.daw.dicepoker.http

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.dicepoker.domain.games.AdvanceRoundError
import pt.isel.daw.dicepoker.domain.games.GameCreationError
import pt.isel.daw.dicepoker.domain.games.GameGetError
import pt.isel.daw.dicepoker.domain.games.GameJoinError
import pt.isel.daw.dicepoker.domain.games.GameLeaveError
import pt.isel.daw.dicepoker.domain.games.GameListError
import pt.isel.daw.dicepoker.domain.games.GameStartError
import pt.isel.daw.dicepoker.domain.games.PlayError
import pt.isel.daw.dicepoker.domain.users.AuthenticatedUser
import pt.isel.daw.dicepoker.http.model.Problem
import pt.isel.daw.dicepoker.http.model.game.GameCreateInputModel
import pt.isel.daw.dicepoker.http.model.game.PlayInput
import pt.isel.daw.dicepoker.http.model.game.PlayInputModel
import pt.isel.daw.dicepoker.services.EventService
import pt.isel.daw.dicepoker.services.GameService
import pt.isel.daw.dicepoker.utils.Failure
import pt.isel.daw.dicepoker.utils.Success
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

private val objectMapper = jacksonObjectMapper()

@RestController
class GameController(
    private val gameService: GameService,
    private val eventService: EventService,
) {

    @GetMapping(Uris.Game.GET_BY_ID)
    fun getGameById(
        user: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        return when (val res = gameService.getGameById(id)) {
            is Success -> {
                ResponseEntity.status(200)
                    .body(res.value)
            }
            is Failure -> {
                when (res.value) {
                    GameGetError.GameDoesNotExist -> Problem.response(400, Problem.gameDoesNotExist)
                    else -> Problem.response(500, Problem.somethingWentWrong)
                }
            }
        }
    }

    @PostMapping(Uris.Game.CREATE)
    fun createGame(
        user: AuthenticatedUser,
        @RequestBody body: GameCreateInputModel,
    ): ResponseEntity<*> {
        val res =
            gameService.createGame(
                body.name,
                body.description,
                body.rounds,
                body.maxPlayers ?: 6,
                body.minPlayers ?: 1, //TODO CHANGE BACK TO 2
                body.timeout ?: 0, //TODO CHANGE BACK TO 100
                user.user.id,
                username = user.user.username,
            )
        return when (res) {
            is Success -> {
                val jsonString = objectMapper.writeValueAsString(res.value)
                eventService.sendMessage(jsonString, res.value.id)
                ResponseEntity.status(201)
                    .header(
                        "Location",
                        Uris.Game.byId(res.value.id).toASCIIString(),
                    )
                    .body(
                        res.value,
                    )
            }
            is Failure -> {
                when (res.value) {
                    GameCreationError.PlayerCantCreateMoreGames -> Problem.response(400, Problem.userMaxConcurrentGamesExceed)
                }
            }
        }
    }

    @PutMapping(Uris.Game.JOIN)
    fun joinGame(
        user: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val res =
            gameService.joinGame(
                id,
                user.user.id,
            )
        when (res) {
            is Success -> {
                val jsonString = objectMapper.writeValueAsString(res.value)
                eventService.sendMessage(jsonString, res.value.id)
                return ResponseEntity.status(200)
                    .header(
                        "Location",
                        Uris.Game.byId(res.value.id).toASCIIString(),
                    )
                    .body(
                        res.value,
                    )
            }
            is Failure -> {
                return when (res.value) {
                    GameJoinError.GameDoesNotExist -> Problem.response(400, Problem.gameDoesNotExist)
                    GameJoinError.PlayerCantJoinMoreGames -> Problem.response(400, Problem.userMaxConcurrentGamesExceed)
                    GameJoinError.GameIsFull -> Problem.response(400, Problem.gameIsFull)
                    GameJoinError.PlayerAlreadyInGame -> Problem.response(400, Problem.userAlreadyInGame)
                    GameJoinError.GameAlreadyInProgress -> Problem.response(400, Problem.gameAlreadyStarted)
                }
            }
        }
    }

    @GetMapping(Uris.Game.LIST)
    fun listGame(
        @RequestParam(value = "order", required = false, defaultValue = "asc") order: String,
        @RequestParam(value = "limit", required = false, defaultValue = "10") limit: Int,
        @RequestParam(value = "status", required = false, defaultValue = "") status: String,
        @RequestParam(value = "lastGameId", required = false, defaultValue = "0") lastGameId: Int,
        ): ResponseEntity<*> {
        val res = gameService.listGames(
            order = order,
            limit = limit,
            status = status.ifBlank { "OPEN" },
            lastGameId = if (lastGameId != 0) lastGameId else null,
        )

        return when (res) {
            is Success -> {
                ResponseEntity.status(200)
                    .body(res.value)
            }

            is Failure -> {
                when (res.value) {
                    GameListError.Failure -> Problem.response(500, Problem.somethingWentWrong)
                }
            }
        }
    }

    @PutMapping(Uris.Game.LEAVE)
    fun leaveGame(
        user: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val res =
            gameService.leaveGame(
                id,
                user.user.id,
            )
        return when (res) {
            is Success -> {
                val jsonString = objectMapper.writeValueAsString(res.value)
                eventService.sendMessage(jsonString, res.value.id)
                ResponseEntity.status(200)
                    .body(res.value)
            }
            is Failure -> {
                when (res.value) {
                    GameLeaveError.UserNotInGame -> Problem.response(400, Problem.userNotInGame)
                    GameLeaveError.GameStatusClosedOrFinished -> Problem.response(400, Problem.gameStatusClosedOrFinished)
                    else -> {
                        Problem.response(400, Problem.invalidRequestContent)
                    }
                }
            }
        }
    }

    @PutMapping(Uris.Game.START_GAME)
    fun startGame(
        user: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val res =
            gameService.startGame(
                gameId = id,
                userId = user.user.id,
            )
        return when (res) {
            is Success -> {
                val jsonString = objectMapper.writeValueAsString(res.value)
                eventService.sendMessage(jsonString, res.value.id)
                ResponseEntity.status(200)
                    .body(res.value)
            }
            is Failure -> {
                when (res.value) {
                    GameStartError.GameDoesNotExist -> Problem.response(400, Problem.gameDoesNotExist)
                    GameStartError.GameStartRequirementsNotFulfilled -> Problem.response(400, Problem.gameStartConditionNotFufilled)
                    GameStartError.UserIsNotHost -> Problem.response(400, Problem.userIsNotHost)
                    GameStartError.UserNotInGame -> Problem.response(400, Problem.userNotInGame)
                }
            }
        }
    }

    @PutMapping(Uris.Game.NEXT_ROUND)
    fun nextRound(
        user: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val res =
            gameService.applyNextRound(
                id,
                user.user.id,
            )
        return when (res) {
            is Success -> {
                val jsonString = objectMapper.writeValueAsString(res.value)
                eventService.sendMessage(jsonString, res.value.id)
                ResponseEntity.status(200)
                    .body(res.value)
            }
            is Failure -> {
                when (res.value) {
                    AdvanceRoundError.UserNotInGame -> Problem.response(400, Problem.userNotInGame)
                    AdvanceRoundError.GameDoesNotExist -> Problem.response(400, Problem.gameDoesNotExist)
                    AdvanceRoundError.InvalidAction -> Problem.response(400, Problem.invalidAction)
                }
            }
        }
    }

    @PutMapping(Uris.Game.PLAY)
    fun play(
        user: AuthenticatedUser,
        @RequestBody playInputModel: PlayInputModel,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val action = PlayInput.valueOf(playInputModel.playAction.uppercase())
        return when (action) {
            PlayInput.END -> {
                val res =
                    gameService.applyEndTurn(
                        id,
                        user.user.id,
                    )
                when (res) {
                    is Success -> {
                        val jsonString = objectMapper.writeValueAsString(res.value)
                        eventService.sendMessage(jsonString, res.value.id)
                        ResponseEntity.status(200).body(res.value)
                    }
                    is Failure -> {
                        when (res.value) {
                            PlayError.GameDoesNotExist -> Problem.response(400, Problem.gameDoesNotExist)
                            PlayError.UserNotInGame -> Problem.response(400, Problem.userNotInGame)
                            PlayError.NotUserTurn -> Problem.response(400, Problem.notUserTurn)
                            PlayError.InvalidAction -> Problem.response(400, Problem.invalidAction)
                            else -> {
                                ResponseEntity.status(500).build<Unit>()
                            }
                        }
                    }
                }
            }
            PlayInput.ROLL -> {
                if (playInputModel.dices != null) {
                    val res =
                        gameService.applyRoll(
                            gameId = id,
                            userId = user.user.id,
                            listOfDices = playInputModel.dices,
                            // TODO ALSO CHANGE THIS LATER
                        )
                    when (res) {
                        is Success -> {
                            val jsonString = objectMapper.writeValueAsString(res.value)
                            eventService.sendMessage(jsonString, res.value.id)
                            ResponseEntity.status(200).body(res.value)
                        }
                        is Failure -> {
                            when (res.value) {
                                PlayError.GameDoesNotExist -> Problem.response(400, Problem.gameDoesNotExist)
                                PlayError.UserNotInGame -> Problem.response(400, Problem.userNotInGame)
                                PlayError.NotUserTurn -> Problem.response(400, Problem.notUserTurn)
                                PlayError.InvalidDiceToRoll -> Problem.response(400, Problem.invalidDiceToRoll)
                                PlayError.InvalidAction -> Problem.response(400, Problem.invalidAction)
                                else -> {
                                    ResponseEntity.status(500).build<Unit>()
                                }
                            }
                        }
                    }
                } else {
                    Problem.response(400, Problem.invalidRequestContent)
                }
            }
            PlayInput.BET -> {
                val res =
                    gameService.applyBet(
                        gameId = id,
                        userId = user.user.id,
                        ante = playInputModel.bet,
                    )
                when (res) {
                    is Success -> {
                        val jsonString = objectMapper.writeValueAsString(res.value)
                        eventService.sendMessage(jsonString, res.value.id)
                        ResponseEntity.status(200)
                            .body(res.value)
                    }
                    is Failure -> {
                        when (res.value) {
                            PlayError.GameDoesNotExist -> Problem.response(400, Problem.gameDoesNotExist)
                            PlayError.UserNotInGame -> Problem.response(400, Problem.userNotInGame)
                            PlayError.AlreadyPlacedBet -> Problem.response(400, Problem.alreadyPlacedBet)
                            PlayError.InsufficientFunds -> Problem.response(400, Problem.insufficientFunds)
                            PlayError.InvalidAction -> Problem.response(400, Problem.invalidAction)
                            PlayError.InvalidBet -> Problem.response(400, Problem.invalidBet)
                            else -> ResponseEntity.status(500).build<Unit>()
                        }
                    }
                }
            }
        }
    }

    @GetMapping(Uris.Game.NEXT_GAME_STATE)
    fun getNextState(
        user: AuthenticatedUser,
        @PathVariable id: Int,
    ) {
        val res = gameService.updateGameStateOnTimeout(
            gameId = id,
        )
        when (res) {
            is Success -> {
                val jsonString = objectMapper.writeValueAsString(res.value)
                eventService.sendMessage(jsonString, res.value.id)
                ResponseEntity.status(200).body(res.value)
            }
            is Failure -> {
                when (res.value) {
                    PlayError.GameDoesNotExist -> Problem.response(400, Problem.gameDoesNotExist)
                    else -> ResponseEntity.status(500).build<Unit>()
                }
            }
        }
    }


    companion object {
        private val logger = LoggerFactory.getLogger(GameController::class.java)
    }
}
