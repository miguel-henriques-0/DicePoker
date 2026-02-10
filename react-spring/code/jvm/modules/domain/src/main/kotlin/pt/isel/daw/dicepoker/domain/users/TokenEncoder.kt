package pt.isel.daw.dicepoker.domain.users

interface TokenEncoder {
    fun createValidationInformation(token: String): TokenValidationInfo
}
