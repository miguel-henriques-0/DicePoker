package pt.isel.daw.dicepoker.domain.games

import pt.isel.daw.dicepoker.utils.Either

sealed class GameCreationError {
    data object PlayerCantCreateMoreGames : GameCreationError()
}
typealias GameCreationResult = Either<GameCreationError, Game>

sealed class GameJoinError {
    data object GameDoesNotExist : GameJoinError()

    data object GameIsFull : GameJoinError()

    data object PlayerCantJoinMoreGames : GameJoinError()

    data object PlayerAlreadyInGame : GameJoinError()

    data object GameAlreadyInProgress : GameJoinError()
}
typealias GameJoinResult = Either<GameJoinError, Game>

sealed class GameListError {
    data object Failure : GameListError()
}
typealias GameListResult = Either<GameListError, GameList>

sealed class GameLeaveError {
    data object UserNotInGame : GameLeaveError()

    data object GameStatusClosedOrFinished : GameLeaveError()

    data object GameDoesNotExist : GameLeaveError()

    data object InvalidAction : GameLeaveError()
}
typealias GameLeaveResult = Either<GameLeaveError, Game>

sealed class GameStartError {
    data object UserNotInGame : GameStartError()

    data object GameDoesNotExist : GameStartError()

    data object GameStartRequirementsNotFulfilled : GameStartError()

    data object UserIsNotHost : GameStartError()
}
typealias GameStartResult = Either<GameStartError, Game>

sealed class PlayError {
    data object UserNotInGame : PlayError()

    data object GameDoesNotExist : PlayError()

    data object NotUserTurn : PlayError()

    data object InvalidDiceToRoll : PlayError()

    data object AlreadyPlacedBet : PlayError()

    data object InsufficientFunds : PlayError()

    data object InvalidAction : PlayError()

    data object InvalidBet : PlayError()
}
typealias PlayResult = Either<PlayError, Game>

sealed class AdvanceRoundError {
    data object InvalidAction : AdvanceRoundError()

    data object GameDoesNotExist : AdvanceRoundError()

    data object UserNotInGame : AdvanceRoundError()
}
typealias AdvanceRoundResult = Either<AdvanceRoundError, Game>


sealed class GameGetError {
    data object GameDoesNotExist : GameGetError()
}
typealias GameGetResult = Either<GameGetError, Game>