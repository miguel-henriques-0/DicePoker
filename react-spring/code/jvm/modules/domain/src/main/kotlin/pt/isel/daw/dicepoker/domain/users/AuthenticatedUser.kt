package pt.isel.daw.dicepoker.domain.users

class AuthenticatedUser(
    val user: User,
    val token: String,
)
