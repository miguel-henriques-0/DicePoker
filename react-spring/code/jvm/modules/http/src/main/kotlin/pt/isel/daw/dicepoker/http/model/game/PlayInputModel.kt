package pt.isel.daw.dicepoker.http.model.game

data class PlayInputModel(
    val dices: List<Int>?,
    val playAction: String,
    val bet: Int?,
)

enum class PlayInput(val action: String) {
    ROLL("roll"),
    END("end"),
    BET("bet"),
}
