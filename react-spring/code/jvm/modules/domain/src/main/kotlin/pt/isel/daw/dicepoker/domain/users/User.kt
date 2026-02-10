package pt.isel.daw.dicepoker.domain.users

data class User(
    val id: Int,
    val username: String,
    val passwordValidation: PasswordValidationInfo,
)
