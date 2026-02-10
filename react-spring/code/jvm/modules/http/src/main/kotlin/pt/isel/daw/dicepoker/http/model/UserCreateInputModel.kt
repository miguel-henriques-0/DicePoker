package pt.isel.daw.dicepoker.http.model

data class UserCreateInputModel(
    val username: String,
    val password: String,
    val inviteCode: String,
)
