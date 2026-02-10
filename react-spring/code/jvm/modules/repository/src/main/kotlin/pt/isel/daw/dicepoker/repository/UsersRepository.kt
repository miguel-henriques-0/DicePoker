package pt.isel.daw.dicepoker.repository

import kotlinx.datetime.Instant
import pt.isel.daw.dicepoker.domain.invite.InviteCode
import pt.isel.daw.dicepoker.domain.users.PasswordValidationInfo
import pt.isel.daw.dicepoker.domain.users.Token
import pt.isel.daw.dicepoker.domain.users.TokenValidationInfo
import pt.isel.daw.dicepoker.domain.users.User

interface UsersRepository {
    fun storeUser(
        username: String,
        passwordValidation: PasswordValidationInfo,
    ): Int

    fun getUserByUsername(username: String): User?

    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>?

    fun isUserStoredByUsername(username: String): Boolean

    fun createToken(
        token: Token,
        maxTokens: Int,
    )

    fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    )

    fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int

    fun createInviteCode(
        userId: Int,
        inviteCodeLimit: Int,
    ): InviteCode?

    fun isInviteCodeValid(inviteCode: Long): Boolean

    fun updateInviteCodeAsUsed(
        inviteCode: Long,
        usedBy: Int,
    ): Boolean
}
