package pt.isel.daw.dicepoker.http.model

import org.springframework.http.ResponseEntity
import java.net.URI

private const val PROBLEM_URI_PATH =
    "https://github.com/isel-leic-daw/2025-daw-leic51n-2025-leic51n-10/blob/main/code/jvm/modules/services/docs/problem"

class Problem(
    typeUri: URI,
) {
    val type = typeUri.toASCIIString()
    val title = typeUri.toString().split("/").last()

    companion object {
        const val MEDIA_TYPE = "application/problem+json"

        fun response(
            status: Int,
            problem: Problem,
        ) = ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body<Any>(problem)

        val userAlreadyExists =
            Problem(
                URI(
                    "$PROBLEM_URI_PATH/user-already-exists",
                ),
            )
        val insecurePassword =
            Problem(
                URI(
                    "$PROBLEM_URI_PATH/insecure-password",
                ),
            )

        val userOrPasswordAreInvalid =
            Problem(
                URI(
                    "$PROBLEM_URI_PATH/user-or-password-are-invalid",
                ),
            )

        val invalidInviteCode =
            Problem(
                URI(
                    "$PROBLEM_URI_PATH/invalid-invite-code",
                ),
            )

        val invalidRequestContent =
            Problem(
                URI(
                    "$PROBLEM_URI_PATH/invalid-request-content",
                ),
            )

        val gameDoesNotExist =
            Problem(
                URI("$PROBLEM_URI_PATH/game-does-not-exists"),
            )

        val userMaxConcurrentGamesExceed =
            Problem(
                URI("$PROBLEM_URI_PATH/user-cant-participate-in-more-games"),
            )

        val gameIsFull =
            Problem(
                URI("$PROBLEM_URI_PATH/game-is-full"),
            )

        val userNotInGame =
            Problem(
                URI("$PROBLEM_URI_PATH/user-does-not-participate-in-specified-game"),
            )

        val gameStatusClosedOrFinished =
            Problem(
                URI("$PROBLEM_URI_PATH/cant-abandon-non-running-game"),
            )

        val notUserTurn =
            Problem(
                URI("$PROBLEM_URI_PATH/not-user-turn"),
            )

        val invalidDiceToRoll =
            Problem(
                URI("$PROBLEM_URI_PATH/invalid-dice-to-roll"),
            )

        val maxRollsReached =
            Problem(
                URI("$PROBLEM_URI_PATH/max-rolls-reached"),
            )

        val inviteLimitMaxReached =
            Problem(
                URI("$PROBLEM_URI_PATH/invite-limit-max-reached"),
            )

        val userAlreadyInGame =
            Problem(
                URI("$PROBLEM_URI_PATH/user-already-in-game"),
            )

        val gameStartConditionNotFufilled =
            Problem(
                URI("$PROBLEM_URI_PATH/start-condition-not-fufilled"),
            )

        val userIsNotHost =
            Problem(
                URI("$PROBLEM_URI_PATH/user-is-not-host"),
            )

        val gameAlreadyStarted =
            Problem(
                URI("$PROBLEM_URI_PATH/game-already-started"),
            )

        val alreadyPlacedBet =
            Problem(
                URI("$PROBLEM_URI_PATH/already-placed-bet"),
            )

        val insufficientFunds =
            Problem(
                URI("$PROBLEM_URI_PATH/insufficient-funds"),
            )

        val invalidAction =
            Problem(
                URI("$PROBLEM_URI_PATH/invalid-action-for-current-game-state"),
            )

        val invalidBet =
            Problem(
                URI("$PROBLEM_URI_PATH/invalid-bet"),
            )

        val somethingWentWrong =
            Problem(
                URI("$PROBLEM_URI_PATH/something-went-wrong"),
            )
    }
}
